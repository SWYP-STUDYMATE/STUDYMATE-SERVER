package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.dto.response.TokenResponse;

public interface TokenService {
    TokenResponse refreshToken(String accessToken);
    void logout(String accessToken);
}
