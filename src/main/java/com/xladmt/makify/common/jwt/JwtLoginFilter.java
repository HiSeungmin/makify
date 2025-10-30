package com.xladmt.makify.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.member.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        log.info("로그인 필터 들어옴");

        try {
            // 스트림을 한 번만 읽고 문자열로 저장
            String raw = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            log.info("🚨 [RAW BODY] " + raw);
            log.info("Content-Type: " + request.getContentType());

            // 문자열에서 객체로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(raw, LoginRequest.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword());

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        MemberDetails userDetails = (MemberDetails) authResult.getPrincipal();
        String loginId = userDetails.getMember().getLoginId().toString();

        String accessToken = jwtUtil.createAccessToken(loginId);
        String refreshToken = jwtUtil.createRefreshToken();

        // Redis에 토큰 저장 (개선된 네이밍)
        redisTemplate.opsForValue().set("auth:token:access:" + loginId, accessToken, 15, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("auth:token:refresh:" + loginId, refreshToken, 30, TimeUnit.DAYS);

        log.info("토큰 발급: loginId={}", loginId);

        // 쿠키 생성
        Cookie accessCookie = new Cookie("access-token", accessToken);
        accessCookie.setHttpOnly(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15);

        Cookie refreshCookie = new Cookie("refresh-token", refreshToken);
        refreshCookie.setHttpOnly(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 30);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("로그인 실패: " + failed.getMessage());
    }
}
