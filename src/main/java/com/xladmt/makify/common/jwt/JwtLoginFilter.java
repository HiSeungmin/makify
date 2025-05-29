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
import org.springframework.security.core.Authentication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new RuntimeException("지원하지 않는 인증 방식입니다: " + request.getMethod());
        }

        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            if (loginRequest.getLoginId() == null || loginRequest.getPassword() == null) {
                throw new RuntimeException("아이디 또는 비밀번호 누락");
            }

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
        String userId = userDetails.getMember().getId().toString();

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(userId);
        String refreshToken = jwtUtil.createRefreshToken();

        // Refresh 토큰 Redis 저장 (30일)
        redisTemplate.opsForValue().set(
                "auth:refresh:" + userId,
                refreshToken,
                30,
                TimeUnit.DAYS
        );

        // 쿠키로 전달
        Cookie accessCookie = new Cookie("access-token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15); // 15분

        Cookie refreshCookie = new Cookie("refresh-token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 30); // 30일

        accessCookie.setSecure(false);
        refreshCookie.setSecure(false);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);
        log.info("Access Cookie: {}", accessCookie.getName() + "=" + accessCookie.getValue());
        log.info("Refresh Cookie: {}", refreshCookie.getName() + "=" + refreshCookie.getValue());

        // 상태 코드 200 OK
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
