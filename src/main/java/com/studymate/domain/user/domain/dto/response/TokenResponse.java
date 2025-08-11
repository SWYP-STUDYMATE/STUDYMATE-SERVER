package com.studymate.domain.user.domain.dto.response;

import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UUID userId
) {
    public static TokenResponse of(String accessToken, String refreshToken, UUID userId){
        return new TokenResponse(accessToken, refreshToken, "Bearer", 3600L, userId);
    }

}
