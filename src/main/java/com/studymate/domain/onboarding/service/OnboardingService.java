package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.user.domain.dto.response.OnboardingStatusResponse;

import java.util.UUID;

public interface OnboardingService {
    OnboardingDataResponse getOnboardingData(UUID userId);
    void completeAllOnboarding(UUID userId, CompleteAllOnboardingRequest request);
    OnboardingStatusResponse getOnboardingProgress(UUID userId);
}