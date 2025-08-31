package com.studymate.domain.user.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.studymate.domain.user.api.GoogleApi;
import com.studymate.domain.user.api.NaverApi;
import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.domain.dto.response.GoogleTokenResponse;
import com.studymate.domain.user.domain.dto.response.GoogleUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.domain.type.UserIdentityType;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.oauth.GoogleUserInfo;
import com.studymate.domain.user.oauth.NaverUserInfo;
import com.studymate.domain.user.oauth.OAuthUserInfo;
import com.studymate.auth.jwt.JwtUtils;
import com.studymate.redis.entity.RefreshToken;
import com.studymate.redis.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final NaverApi naverApi;
    private final GoogleApi googleApi;
    private final JwtUtils jwtUtils;
    private final UserDao userDao;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String getLoginUrl(String provider, String state, String clientId, String redirectUri) {
        switch (UserIdentityType.valueOf(provider.toUpperCase())) {
            case NAVER -> {
                return UriComponentsBuilder.fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("state", state)
                        .build().toUriString();
            }
            case GOOGLE -> {
                return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("scope", "openid profile email")
                        .queryParam("state", state)
                        .build().toUriString();
            }
            default -> throw new IllegalArgumentException("없는 provider" + provider);
        }
    }

    @Override

    public TokenResponse getLoginTokenCallback(String provider, String code, String state) {
        OAuthUserInfo userInfo;
        UserIdentityType type = UserIdentityType.valueOf(provider.toUpperCase());
        if (type == UserIdentityType.NAVER) {
            NaverTokenResponse token = naverApi.getToken(code, state);
            NaverUserInfoResponse res = naverApi.getUserInfo(token.access_token());
            userInfo = new NaverUserInfo(res);
        } else if (type == UserIdentityType.GOOGLE) {
            GoogleTokenResponse token = googleApi.getToken(code);
            GoogleUserInfoResponse res = googleApi.getUserInfo(token.access_token());
            userInfo = new GoogleUserInfo(res);
        } else {
            throw new IllegalArgumentException("없는 provider" + provider);

        }

        User user = userDao.findByUserIdentity(userInfo.getId())
                .map(existingUser -> {
                    if (type == UserIdentityType.NAVER) {
                        existingUser.updateNaverProfile(userInfo.getName(), null, null, userInfo.getProfileImageUrl());
                    } else if (type == UserIdentityType.GOOGLE) {
                        existingUser.updateGoogleProfile(userInfo.getName(), userInfo.getProfileImageUrl());
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .userIdentity(userInfo.getId())
                            .userIdentityType(type)
                            .userCreatedAt(LocalDateTime.now())
                            .name(userInfo.getName())
                            .profileImage(userInfo.getProfileImageUrl())
                            .userDisable(false)
                            .build();
                    return newUser;
                });

        userDao.save(user);

        UUID userId = user.getUserId();
        String accessToken = jwtUtils.generateAccessToken(userId);
        String refreshToken = jwtUtils.generateRefreshToken(userId);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId.toString())
                        .token(refreshToken)
                        .ttlSeconds(TimeUnit.DAYS.toSeconds(7))
                        .build());

        return TokenResponse.of(accessToken, refreshToken, userId);
    }
}
