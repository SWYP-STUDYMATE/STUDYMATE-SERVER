package com.studymate.domain.user.domain.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken){
        return new TokenResponse(accessToken,refreshToken,"Bearer",3600L);
    }

}
