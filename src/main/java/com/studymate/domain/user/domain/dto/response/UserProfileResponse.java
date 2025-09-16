package com.studymate.domain.user.domain.dto.response;

import com.studymate.domain.onboard.domain.dto.response.LanguageResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        @JsonProperty("id")
        String id,  // Changed from UUID userId to String id to match client
        String englishName,
        @JsonProperty("profileImageUrl")
        String profileImageUrl,  // Changed from profileImage to match client
        String selfBio,
        LocationResponse location,
        LanguageResponse nativeLanguage,  // Added missing field
        List<LanguageResponse> targetLanguages,  // Added missing field
        @JsonProperty("birthYear")
        Integer birthYear,  // Changed from String birthyear to Integer birthYear
        String birthday,
        UserGenderTypeResponse gender,  // Changed from UserGenderType to UserGenderTypeResponse
        LocalDateTime createdAt,  // Added missing field
        LocalDateTime updatedAt   // Added missing field
) {
    // Helper constructor for backward compatibility
    public UserProfileResponse(UUID userId, String englishName, String profileImage, 
                              String selfBio, LocationResponse location, 
                              LanguageResponse nativeLanguage, List<LanguageResponse> targetLanguages,
                              String birthyear, String birthday, UserGenderTypeResponse gender,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(userId.toString(), englishName, profileImage, selfBio, location, 
             nativeLanguage, targetLanguages, 
             birthyear != null ? Integer.parseInt(birthyear) : null, 
             birthday, gender, createdAt, updatedAt);
    }
}