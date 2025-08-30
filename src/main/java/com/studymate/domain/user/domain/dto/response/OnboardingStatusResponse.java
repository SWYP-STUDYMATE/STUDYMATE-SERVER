package com.studymate.domain.user.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingStatusResponse {
    private boolean basicInfoCompleted;
    private boolean languageInfoCompleted;
    private boolean interestInfoCompleted;
    private boolean partnerInfoCompleted;
    private boolean scheduleInfoCompleted;
    private boolean onboardingCompleted;
    private int currentStep;
    private int totalSteps;
}