package com.studymate.domain.user.service;

import com.studymate.domain.user.api.NaverApi;
import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.domain.dto.response.LoginTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverTokenResponse;
import com.studymate.domain.user.domain.dto.response.NaverUserInfoResponse;
import com.studymate.domain.user.domain.dto.response.TokenResponse;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.util.JwtUtils;
import com.studymate.exception.LoginExpirationException;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final NaverApi naverApi;
    private final JwtUtils jwtUtils;
    private final UserDao userDao;

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
    public String getLoginTokenCallback(String code, String state) {
        NaverTokenResponse token = naverApi.getToken(code, state);
        NaverUserInfoResponse userInfo = naverApi.getUserInfo(token.access_token());
        Optional<User> OptionalUser = userDao.findByUserIdentity(userInfo.id());
        User user;
        if (OptionalUser.isPresent()) {
            user = OptionalUser.get();
            user.updateNaverProfile(userInfo.name(), userInfo.birthday(), userInfo.birthyear(), userInfo.gender());
        } else {
            user = User.builder()
                    .userIdentity(userInfo.id())
                    .userCreatedAt(LocalDateTime.now())
                    .name(userInfo.name())
                    .birthday(userInfo.birthday())
                    .gender(userInfo.gender())
                    .birthyear(userInfo.birthyear())
                    .userDisable(true)
                    .build();
        }

        userDao.save(user);
        return jwtUtils.createLoginToken(new LoginTokenResponse(user.getUserId()));

    }

    @Override
    public TokenResponse generateTokens(String identity) {
        User user = userDao.findByUserId(UUID.fromString(identity))
                .orElseThrow(() -> new NotFoundException("USER NOT FOUND"));

        LoginTokenResponse loginTokenResponse = new LoginTokenResponse(user.getUserId());
        String accessToken = jwtUtils.createLoginToken(loginTokenResponse);
        String refreshToken = jwtUtils.createRefreshToken(loginTokenResponse);

        return TokenResponse.of(accessToken,refreshToken);
    }
    @Override
    public String generateLoginToken(String identity){
        User user = userDao.findByUserIdentity(identity)
                .orElseThrow(() -> new NotFoundException("USER NOT FOUND"));
        LoginTokenResponse loginTokenResponse = new LoginTokenResponse(user.getUserId());
        return jwtUtils.createLoginToken(loginTokenResponse);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        if(jwtUtils.isTokenExpired(refreshToken)) {
            throw new LoginExpirationException();
        }
        LoginTokenResponse loginTokenResponse = jwtUtils.parseRefreshToken(refreshToken);

        String newAccessToken = jwtUtils.createLoginToken(loginTokenResponse);
        String newRefreshToken = jwtUtils.createRefreshToken(loginTokenResponse);
        return TokenResponse.of(newAccessToken,newRefreshToken);


    }




}
