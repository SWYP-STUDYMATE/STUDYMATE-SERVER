package com.studymate.domain.user.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteOnboardingRequest {
    // 언어 정보
    private Integer nativeLanguageId;
    private List<LanguageLevelData> targetLanguages;
    
    // 관심사 정보  
    private List<Integer> motivationIds;
    private List<Integer> topicIds;
    private List<Integer> learningStyleIds;
    private List<Integer> learningExpectationIds;
    
    // 파트너 선호도
    private List<Integer> partnerPersonalityIds;
    private List<Integer> groupSizeIds;
    
    // 스케줄 정보
    private List<Integer> scheduleIds;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LanguageLevelData {
        private Integer languageId;
        private Integer currentLevelId;
        private Integer targetLevelId;
    }
}