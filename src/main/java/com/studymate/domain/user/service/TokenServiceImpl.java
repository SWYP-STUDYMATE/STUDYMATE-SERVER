package com.studymate.domain.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.auth.jwt.JwtUtils;
import com.studymate.exception.LoginExpirationException;
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
        // 4) 새 Access Token 발급
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        return TokenResponse.of(newAccessToken, null, userId);
    }

    @Override
    public void logout(String accessToken) {
        if (!jwtUtils.validateToken(accessToken))
            return;
        UUID userId = jwtUtils.getUserIdFromToken(accessToken);
        refreshTokenRepository.deleteById(userId.toString());
    }
}
