package com.studymate.domain.onboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardStatusResponse {
    private Integer currentStep;
    private Integer totalSteps;
    private Double progressPercentage;
    private boolean completed;
    private String status;
    private Integer estimatedMinutesRemaining;
}