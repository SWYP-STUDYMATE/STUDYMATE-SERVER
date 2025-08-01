package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;

public interface LoginService {
    String getLoginUrl (String provider,String state, String clientId, String redirectUrl);
    TokenResponse getLoginTokenCallback(String provider,String code, String state);

}
