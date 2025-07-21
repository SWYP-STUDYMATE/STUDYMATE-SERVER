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
    public TokenResponse refreshToken(String refreshtoken) {

            LoginTokenResponse loginTokenResponse = jwtUtils.parseRefreshToken(refreshtoken);
            String userId = loginTokenResponse.uuid().toString();

            String key = "refresh_token:" + userId;
            String storedRefreshToken = redisTemplate.opsForValue().get(key);

            if (storedRefreshToken == null) {
                throw new LoginExpirationException();
            }

            if (jwtUtils.isTokenExpired(storedRefreshToken)) {
                redisTemplate.delete(key);
                throw new LoginExpirationException();
            }

            String newAccessToken = jwtUtils.createLoginToken(loginTokenResponse);

            return TokenResponse.of(newAccessToken, null);
    }


    @Override
    public void logout(String accessToken) {
            LoginTokenResponse loginTokenResponse = jwtUtils.parseLoginToken(accessToken);
            String userId = loginTokenResponse.uuid().toString();

            String key = "refresh_token:" + userId;
            redisTemplate.delete(key);


    }
}
