package com.xladmt.makify.member.controller;

import com.xladmt.makify.common.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractTokenFromCookies(request, "refresh-token");

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh token이 유효하지 않음");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        String redisToken = redisTemplate.opsForValue().get("auth:refresh:" + userId);

        if (redisToken == null || !redisToken.equals(refreshToken)) {
            return ResponseEntity.status(401).body("이미 만료된 리프레시 토큰");
        }

        String newAccessToken = jwtUtil.createAccessToken(userId);

        Cookie accessCookie = new Cookie("access-token", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15); // 15분

        response.addCookie(accessCookie);

        return ResponseEntity.ok("access token 재발급 완료");
    }

    private String extractTokenFromCookies(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
