package com.studymate.domain.user.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.auth.jwt.JwtUtils;
import com.studymate.exception.LoginExpirationException;
import com.studymate.redis.entity.RefreshToken;
import com.studymate.redis.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 1) 유효성 검사
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new LoginExpirationException();
        }
        // 2) 토큰에서 userId 추출
        UUID userId = jwtUtils.getUserIdFromToken(refreshToken);
        // 3) Redis 에 저장된 리프레시 토큰과 비교
        String stored = refreshTokenRepository.findById(userId.toString())
                .map(rt -> rt.getToken())
                .orElse(null);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new LoginExpirationException();
        }
        // 4) 새 토큰 발급 및 저장
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);
        
        // 5) 새 리프레시 토큰을 Redis에 저장 (기존 토큰 교체)
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId.toString())
                        .token(newRefreshToken)
                        .ttlSeconds(TimeUnit.DAYS.toSeconds(7))
                        .build());
        
        return TokenResponse.of(newAccessToken, newRefreshToken, userId);
    }

    @Override
    public void logout(String accessToken) {
        if (!jwtUtils.validateToken(accessToken))
            return;
        UUID userId = jwtUtils.getUserIdFromToken(accessToken);
        refreshTokenRepository.deleteById(userId.toString());
    }
}
