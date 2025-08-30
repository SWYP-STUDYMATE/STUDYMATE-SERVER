package com.studymate.domain.user.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCompleteProfileResponse {
    private String englishName;
    private String koreanName;
    private String profileImageUrl;
    private String selfBio;
    private String email;
    private String gender;
    private Integer birthYear;
    private String birthday;
    private String location;
    private String nativeLanguage;
    private String targetLanguage;
    private String languageLevel;
    private boolean onboardingCompleted;
}