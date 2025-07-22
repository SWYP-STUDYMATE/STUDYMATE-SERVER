package com.studymate.domain.user.domain.dto.response;

import com.studymate.domain.user.entity.User;
import io.jsonwebtoken.Claims;

import java.util.UUID;

public record LoginTokenResponse(UUID uuid) {
    public static LoginTokenResponse from(User user){
        return new LoginTokenResponse(user.getUserId());
    }
    public static LoginTokenResponse from(Claims claims){
        return new LoginTokenResponse(
                UUID.fromString(claims.get("uuid",String.class))
        );
    }

}
