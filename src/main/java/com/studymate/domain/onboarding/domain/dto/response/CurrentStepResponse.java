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
public class CurrentStepResponse {
    
    // 현재 단계 정보
    private Integer currentStepNumber;
    private String currentStepName;
    private String currentStepDescription;
    
    // 진행 상황
    private Double overallProgress; // 0.0 ~ 1.0
    private Integer completedSteps;
    private Integer totalSteps;
    
    // 시간 정보  
    private Integer estimatedMinutesForCurrentStep;
    private Integer estimatedMinutesTotal;
    private LocalDateTime sessionStartedAt;
    
    // 현재 단계 가이드
    private List<String> instructions;
    private Map<String, String> fieldHelpTexts;
    private List<String> tips;
    
    // 동기부여 메시지
    private String encouragementMessage;
    private String progressMessage; // "거의 다 왔어요!" 같은 메시지
    
    // 단계 제어 옵션
    private Boolean canSkip;
    private String skipReason;
    private Boolean canGoBack;
    private Boolean isRequired;
    
    // 저장된 데이터 정보
    private Map<String, Object> savedData;
    private Boolean hasUnsavedChanges;
    private LocalDateTime lastSavedAt;
    
    // 다음 단계 미리보기
    private NextStepPreview nextStep;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NextStepPreview {
        private Integer stepNumber;
        private String stepName;
        private String shortDescription;
        private Integer estimatedMinutes;
        private Boolean isRequired;
    }
}