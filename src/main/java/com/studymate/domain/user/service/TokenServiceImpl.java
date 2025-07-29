package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.domain.dto.response.LoginTokenResponse;
import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.util.JwtUtils;
import com.studymate.exception.LoginExpirationException;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserDao userDao;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;



    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 1) 유효성 검사
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new LoginExpirationException();
        }
        // 2) 토큰에서 userId 추출
        UUID userId = jwtUtils.getUserIdFromToken(refreshToken);
        // 3) Redis 에 저장된 리프레시 토큰과 비교
        String stored = redisTemplate.opsForValue().get("refresh_token:" + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new LoginExpirationException();
        }
        // 4) 새 Access Token 발급
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        return TokenResponse.of(newAccessToken, null);
    }

    @Override
    public void logout(String accessToken) {
        if (!jwtUtils.validateToken(accessToken)) return;
        UUID userId = jwtUtils.getUserIdFromToken(accessToken);
        redisTemplate.delete("refresh_token:" + userId);
    }
}
