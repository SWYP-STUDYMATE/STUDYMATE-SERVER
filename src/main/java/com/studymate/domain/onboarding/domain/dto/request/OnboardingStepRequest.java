package com.studymate.domain.onboarding.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingStepRequest {
    
    @NotNull(message = "단계 번호는 필수입니다")
    @Min(value = 1, message = "단계 번호는 1 이상이어야 합니다")
    @Max(value = 7, message = "단계 번호는 7 이하여야 합니다")
    private Integer stepNumber;
    
    @NotNull(message = "단계 데이터는 필수입니다")
    private Map<String, Object> stepData;
    
    // 각 단계별 전용 필드들
    // Step 1: 언어 설정
    private Integer nativeLanguageId;
    private List<TargetLanguageData> targetLanguages;
    
    // Step 2: 학습 동기 및 관심사
    private List<Integer> motivationIds;
    private List<Integer> topicIds;
    
    // Step 3: 학습 스타일
    private List<Integer> learningStyleIds;
    private List<Integer> learningExpectationIds;
    
    // Step 4: 파트너 선호도
    private List<Integer> partnerPersonalityIds;
    private List<Integer> partnerGenderIds;
    
    // Step 5: 그룹 크기 선호도
    private List<Integer> groupSizeIds;
    
    // Step 6: 스케줄 설정
    private List<Integer> scheduleIds;
    private List<Integer> communicationMethodIds;
    private List<Integer> dailyMinuteIds;
    
    // Step 7: 최종 확인 및 검토
    private Boolean agreedToTerms;
    private Boolean allowNotifications;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TargetLanguageData {
        private Integer languageId;
        private Integer currentLevelId;
        private Integer targetLevelId;
    }
}