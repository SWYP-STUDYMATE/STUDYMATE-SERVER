package com.studymate.domain.user.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardStatusResponse {
    private boolean basicInfoCompleted;
    private boolean languageInfoCompleted;
    private boolean interestInfoCompleted;
    private boolean partnerInfoCompleted;
    private boolean scheduleInfoCompleted;
    private boolean onboardingCompleted;
    private int currentStep;
    private int totalSteps;
    
    // 새로운 UX 개선을 위한 필드들
    private double progressPercentage; // 0.0 ~ 100.0
    private boolean isCompleted;
    
    public OnboardStatusResponse(boolean basicInfoCompleted,
                                   boolean languageInfoCompleted,
                                   boolean interestInfoCompleted,
                                   boolean partnerInfoCompleted,
                                   boolean scheduleInfoCompleted,
                                   boolean onboardingCompleted,
                                   int currentStep,
                                   int totalSteps) {
        this.basicInfoCompleted = basicInfoCompleted;
        this.languageInfoCompleted = languageInfoCompleted;
        this.interestInfoCompleted = interestInfoCompleted;
        this.partnerInfoCompleted = partnerInfoCompleted;
        this.scheduleInfoCompleted = scheduleInfoCompleted;
        this.onboardingCompleted = onboardingCompleted;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.progressPercentage = ((double) currentStep / totalSteps) * 100.0;
        this.isCompleted = onboardingCompleted;
    }
}