package com.studymate.domain.user.domain.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(
        @JsonProperty("sub") String sub,
        @JsonProperty("name") String name
) {
}
