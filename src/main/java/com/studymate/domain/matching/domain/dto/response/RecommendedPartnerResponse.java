package com.studymate.domain.matching.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedPartnerResponse {
    private UUID userId;
    private String englishName;
    private String profileImageUrl;
    private String selfBio;
    private Integer age;
    private String gender;
    private String location;
    private String nativeLanguage;
    private List<TargetLanguageInfo> targetLanguages;
    private List<String> interests;
    private List<String> partnerPersonalities;
    private double compatibilityScore;
    private String onlineStatus;
    private String lastActiveTime;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TargetLanguageInfo {
        private String languageName;
        private String currentLevel;
        private String targetLevel;
    }
}