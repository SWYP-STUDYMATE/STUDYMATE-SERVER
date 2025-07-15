package com.studymate.domain.user.domain.dto.response;

public record NaverUserInfoWrapper(
        String resultcode,
        String message,
        NaverUserInfoResponse response
) {
}
