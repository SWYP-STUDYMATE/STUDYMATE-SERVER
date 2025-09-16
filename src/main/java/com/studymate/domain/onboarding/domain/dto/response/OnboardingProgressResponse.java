package com.studymate.domain.onboarding.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingProgressResponse {
    
    // 진행률 정보
    private Integer currentStep;
    private Integer totalSteps;
    private Double progressPercentage;
    private Integer estimatedMinutesRemaining;
    
    // 단계별 완료 상태
    private List<StepStatus> stepStatuses;
    
    // 현재 단계 정보
    private StepMetadata currentStepInfo;
    
    // 다음 단계 미리보기
    private StepMetadata nextStepPreview;
    
    // 사용자 맞춤 안내 메시지
    private String motivationalMessage;
    private String nextStepGuidance;
    
    // 건너뛰기 가능 여부
    private Boolean canSkipCurrentStep;
    private String skipReason;
    
    // 임시 저장 정보
    private LocalDateTime lastSavedAt;
    private Boolean hasUnsavedChanges;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepStatus {
        private Integer stepNumber;
        private String stepName;
        private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED
        private Boolean isRequired;
        private Integer estimatedMinutes;
        private LocalDateTime completedAt;
        private LocalDateTime lastUpdatedAt;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepMetadata {
        private Integer stepNumber;
        private String stepName;
        private String description;
        private String instructions;
        private Boolean isRequired;
        private Integer estimatedMinutes;
        private List<String> requiredFields;
        private List<String> optionalFields;
        private Map<String, String> helpTexts;
        
        // 단계별 혜택 설명
        private String benefitDescription;
        private List<String> exampleOptions;
    }
}