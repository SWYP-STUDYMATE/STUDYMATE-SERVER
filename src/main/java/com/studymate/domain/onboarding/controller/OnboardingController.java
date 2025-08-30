package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.CompleteAllOnboardingRequest;
import com.studymate.domain.onboarding.domain.dto.response.OnboardingDataResponse;
import com.studymate.domain.onboarding.service.OnboardingService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/data")
    public OnboardingDataResponse getOnboardingData(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return onboardingService.getOnboardingData(userId);
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeAllOnboarding(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @RequestBody CompleteAllOnboardingRequest request) {
        UUID userId = principal.getUuid();
        onboardingService.completeAllOnboarding(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getOnboardingProgress(@AuthenticationPrincipal CustomUserDetails principal) {
        UUID userId = principal.getUuid();
        return ResponseEntity.ok(onboardingService.getOnboardingProgress(userId));
    }
}