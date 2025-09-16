package com.studymate.domain.onboard.service;

import com.studymate.domain.onboard.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboard.domain.dto.request.OnboardStepRequest;
import com.studymate.domain.onboard.domain.dto.response.OnboardDataResponse;
import com.studymate.domain.onboard.domain.dto.response.OnboardProgressResponse;
import com.studymate.domain.onboard.domain.dto.response.CurrentStepResponse;
import com.studymate.domain.user.domain.dto.response.OnboardStatusResponse;

import java.util.Map;
import java.util.UUID;

public interface OnboardService {
    // 기존 메서드들
    OnboardDataResponse getOnboardingData(UUID userId);
    void completeAllOnboarding(UUID userId, CompleteAllOnboardingRequest request);
    OnboardStatusResponse getOnboardingProgress(UUID userId);
    
    // 새로운 UX 개선 메서드들
    OnboardProgressResponse saveOnboardingStep(UUID userId, Integer stepNumber, OnboardStepRequest request);
    CurrentStepResponse getCurrentOnboardingStep(UUID userId);
    OnboardProgressResponse skipOnboardingStep(UUID userId, Integer stepNumber, String reason);
    CurrentStepResponse goBackToOnboardingStep(UUID userId, Integer stepNumber);
    void autoSaveOnboardingData(UUID userId, Map<String, Object> currentData);
    Object startTrialMatching(UUID userId);
    void extendOnboardingSession(UUID userId);
}