package com.xladmt.makify.common.aop;

import com.xladmt.makify.common.util.IpTracer;
import com.xladmt.makify.common.util.SensitiveDataMasker;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

/**
 * Controller 레이어의 모든 메서드를 AOP로 로깅
 * 요청/응답, 실행시간, 클라이언트 IP, UserID 등을 기록
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final IpTracer ipTracer;
    private final SensitiveDataMasker sensitiveDataMasker;

    @Value("${jwt.secret:your-secret-key}")
    private String jwtSecret;

    /**
     * Controller의 모든 public 메서드 로깅
     */
    @Around("execution(* com.xladmt.makify..controller..*(..))")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();

        // 1. 요청 정보 수집
        String method = request.getMethod();
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = ipTracer.getClientIp(request);
        String userId = extractUserIdFromJwt(request);
        String requestBody = extractRequestBody(request);
        String authHeader = extractAuthorizationHeader(request);

        // 2. 요청 로그 출력
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("[REQUEST] {}", getCurrentTimestamp());
        log.info("  Method: {} | URI: {}", method, requestUri);
        if (queryString != null && !queryString.isEmpty()) {
            log.info("  Query: {}", queryString);
        }
        log.info("  Client IP: {} | User ID: {}", clientIp, userId != null ? userId : "ANONYMOUS");
        if (authHeader != null) {
            log.info("  Authorization: {}", authHeader);
        }
        if (requestBody != null && !requestBody.isEmpty()) {
            String maskedBody = sensitiveDataMasker.maskJsonString(requestBody);
            log.info("  Request Body: {}", maskedBody);
        }

        // 3. 메서드 실행
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();

            // 4. 응답 로그 출력 (성공)
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[RESPONSE] Status: 200 | Time: {}ms", executionTime);
            if (result != null) {
                String maskedResult = sensitiveDataMasker.maskObject(result);
                log.info("  Response Body: {}", maskedResult);
            }
            log.info("═══════════════════════════════════════════════════════════════");

            return result;

        } catch (Throwable e) {
            // 5. 예외 로그 출력
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("[ERROR] {} | Time: {}ms", e.getClass().getSimpleName(), executionTime);
            log.error("  Exception Message: {}", e.getMessage());
            log.error("  Request: {} {}", method, requestUri);
            log.error("  Client IP: {} | User ID: {}", clientIp, userId != null ? userId : "ANONYMOUS");
            log.error("═══════════════════════════════════════════════════════════════");

            throw e;
        }
    }

    /**
     * HttpServletRequest 가져오기
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("Request context not found");
        }
        return attributes.getRequest();
    }

    /**
     * JWT 토큰에서 userId 추출
     */
    private String extractUserIdFromJwt(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            log.debug("JWT 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Authorization 헤더 추출 및 마스킹
     */
    private String extractAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        return sensitiveDataMasker.maskHeaders(authHeader);
    }

    /**
     * Request Body 추출
     */
    private String extractRequestBody(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();
            if (contentType == null || (!contentType.contains("application/json") && 
                                       !contentType.contains("application/x-www-form-urlencoded"))) {
                return null;
            }

            BufferedReader reader = request.getReader();
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            return requestBody.toString();
        } catch (Exception e) {
            log.debug("Request Body 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 시간 포맷팅
     */
    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
    }
}
