package com.studymate.domain.user.domain.dto.response;

import com.studymate.domain.user.domain.type.UserGenderType;

import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String name,
        String englishName,
        String email,
        String birthday,
        String birthyear,
        UserGenderType gender,
        String profileImage,
        String selfBio,
        LocationResponse location,
        Boolean isOnboardingCompleted
) {
}