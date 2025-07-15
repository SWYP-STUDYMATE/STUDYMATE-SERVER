package com.studymate.domain.user.util;

import com.studymate.domain.user.domain.dto.response.LoginTokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
public class JwtUtils {

    private final SecretKey secretKey;

    public JwtUtils(
            @Value("${jwt.secret_key}") String secret){
                this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String createLoginToken(LoginTokenResponse loginTokenResponse){
        return Jwts.builder()
                .claim("uuid",loginTokenResponse.uuid())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

    }

    public LoginTokenResponse parseLoginToken(String loginToken){
        Claims claims = (Claims) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(loginToken)
                .getPayload();
        return LoginTokenResponse.from(claims);
    }

    public String createRefreshToken(LoginTokenResponse loginTokenResponse){
        return Jwts.builder()
                .claim("uuid",loginTokenResponse.uuid())
                .claim("type","refresh")
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 *3600000))
                .signWith(secretKey)
                .compact();
    }

    public LoginTokenResponse parseRefreshToken(String refreshToken){
        Claims claims = (Claims) Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parse(refreshToken)
                .getPayload();

        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("리프레시 토큰 검증 실패");
        }

        return LoginTokenResponse.from(claims);
    }

    public boolean isTokenExpired(String token){
        try {
            Claims claims = (Claims) Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
