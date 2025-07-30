package com.studymate.domain.user.service;

import com.studymate.domain.user.api.NaverApi;
import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.domain.dto.response.LoginTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final NaverApi naverApi;
    private final JwtUtils jwtUtils;
    private final UserDao userDao;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String getLoginUrl(String state, String clientId, String redirectUri) {
        return UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build().toUriString();
    }

    @Override
    public TokenResponse getLoginTokenCallback(String code, String state) {
        NaverTokenResponse token = naverApi.getToken(code, state);
        NaverUserInfoResponse userInfo = naverApi.getUserInfo(token.access_token());

        Optional<User> optUser = userDao.findByUserIdentity(userInfo.id());
        User user = optUser.orElseGet(() -> {
            User u = User.builder()
                    .userIdentity(userInfo.id())
                    .userCreatedAt(LocalDateTime.now())
                    .name(userInfo.name())
                    .birthday(userInfo.birthday())
                    .gender(userInfo.gender())
                    .birthyear(userInfo.birthyear())
                    .userDisable(false)
                    .build();
            return u;
        });
        userDao.save(user);

        UUID userId = user.getUserId();
        String accessToken = jwtUtils.generateAccessToken(userId);
        String refreshToken = jwtUtils.generateRefreshToken(userId);
        redisTemplate.opsForValue()
                .set("refresh_token:" + userId, refreshToken, 7, TimeUnit.DAYS);

        return TokenResponse.of(accessToken, refreshToken);
    }
}
