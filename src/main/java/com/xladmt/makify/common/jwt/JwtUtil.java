package com.xladmt.makify.common.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30일

    private final Key key;

    public JwtUtil() {
        String secret = "mysecretkeymakifysecuresecretmakifysecuresecret"; // 256bit 이상
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(String loginId) {
        return Jwts.builder()
                .setSubject(loginId)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        return Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // userId 꺼내기
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 만료 시간 가져오기
    public Date getExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    // 만료 여부
    public boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
