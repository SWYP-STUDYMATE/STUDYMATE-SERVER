package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;

public interface LoginService {
    String getLoginUrl (String state, String clientId, String redirectUrl);
    TokenResponse getLoginTokenCallback(String code, String state);

}
