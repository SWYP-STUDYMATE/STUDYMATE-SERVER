package com.studymate.domain.onboarding.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStatusResponse {
    private Integer currentStep;
    private Integer totalSteps;
    private Double progressPercentage;
    private boolean completed;
    private String status;
    private Integer estimatedMinutesRemaining;
}