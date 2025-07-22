package com.studymate.domain.user.domain.dto.response;

public record NaverTokenResponse(
        String token_type,
        String access_token,
        String refresh_token,
        Integer expires_in
) {
}
