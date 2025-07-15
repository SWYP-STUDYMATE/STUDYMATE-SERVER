package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;

public interface LoginService {
    String getLoginUrl (String state, String clientId, String redirectUrl);
    String getLoginTokenCallback(String code, String state);
    String generateLoginToken(String identity);
    TokenResponse generateTokens(String identity);
    TokenResponse refreshToken(String refreshToken);
}
