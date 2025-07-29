package com.studymate.domain.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    private final SecretKey secretKey;
    private static final long ACCESS_TOKEN_VALIDITY  = 1000L * 60 * 60;          // 1시간
    private static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7;  // 7일

    public JwtUtils(@Value("${jwt.secret_key}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String generateAccessToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // 토큰에서 userId(UUID) 추출
    public UUID getUserIdFromToken(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
        return UUID.fromString(jws.getBody().getSubject());
    }

    // 토큰 유효성(서명·만료) 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // HTTP 요청 헤더에서 Bearer 토큰 분리
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
