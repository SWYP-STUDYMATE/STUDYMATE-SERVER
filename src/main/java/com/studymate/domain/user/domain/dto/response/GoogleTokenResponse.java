package com.studymate.domain.user.domain.dto.response;

public record GoogleTokenResponse(
        String access_token,
        String expires_in,
        String id_token,
        String scope,
        String token_type
) {
}
