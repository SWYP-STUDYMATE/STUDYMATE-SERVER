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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        log.debug("Token refresh 요청 시작");
        
        // 1) 유효성 검사
        if (!jwtUtils.validateToken(refreshToken)) {
            log.warn("Refresh token 유효성 검사 실패");
            throw new LoginExpirationException();
        }
        
        // 2) 토큰에서 userId 추출
        UUID userId = jwtUtils.getUserIdFromToken(refreshToken);
        log.debug("Token refresh 요청 - userId: {}", userId);
        
        // 3) Redis 에 저장된 리프레시 토큰과 비교
        String stored = refreshTokenRepository.findById(userId.toString())
                .map(rt -> rt.getToken())
                .orElse(null);
        
        if (stored == null) {
            log.warn("Redis에서 refresh token을 찾을 수 없음 - userId: {}", userId);
            throw new LoginExpirationException();
        }
        
        if (!stored.equals(refreshToken)) {
            log.warn("제공된 refresh token이 저장된 토큰과 불일치 - userId: {}", userId);
            throw new LoginExpirationException();
        }
        
        // 4) 새 토큰 발급 및 저장
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);

        log.info("REFRESH_TOKEN 새로운 토큰 발급 - UserID: {}, NewAccessToken: {}, NewRefreshToken: {}",
                userId, newAccessToken, newRefreshToken);

        // 5) 새 리프레시 토큰을 Redis에 저장 (기존 토큰 교체)
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId.toString())
                        .token(newRefreshToken)
                        .ttlSeconds(TimeUnit.DAYS.toSeconds(7))
                        .build());
        
        log.info("Token refresh 성공 - userId: {}", userId);
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
