package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardingStepRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingProgressResponse;
import com.studymate.domain.onboarding.domain.dto.response.CurrentStepResponse;
import com.studymate.domain.user.domain.dto.response.OnboardingStatusResponse;

import java.util.Map;
import java.util.UUID;

public interface OnboardingService {
    // 기존 메서드들
    OnboardingDataResponse getOnboardingData(UUID userId);
    void completeAllOnboarding(UUID userId, CompleteAllOnboardingRequest request);
    OnboardingStatusResponse getOnboardingProgress(UUID userId);
    
    // 새로운 UX 개선 메서드들
    OnboardingProgressResponse saveOnboardingStep(UUID userId, Integer stepNumber, OnboardingStepRequest request);
    CurrentStepResponse getCurrentOnboardingStep(UUID userId);
    OnboardingProgressResponse skipOnboardingStep(UUID userId, Integer stepNumber, String reason);
    CurrentStepResponse goBackToOnboardingStep(UUID userId, Integer stepNumber);
    void autoSaveOnboardingData(UUID userId, Map<String, Object> currentData);
    Object startTrialMatching(UUID userId);
    void extendOnboardingSession(UUID userId);
}