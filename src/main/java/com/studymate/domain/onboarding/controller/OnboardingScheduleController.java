package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.CommunicationMethodRequest;
import com.studymate.domain.onboarding.domain.dto.request.DailyMinuteRequest;
import com.studymate.domain.onboarding.domain.dto.request.GroupSizeRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardingScheduleRequests;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.dto.response.GroupSizeResponse;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.service.OnboardingScheduleService;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.auth.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/onboarding/schedule")
public class OnboardingScheduleController {

    private final OnboardingScheduleService onboardingScheduleService;
    private final JwtUtils jwtUtils;

    @PostMapping("/communication-method")
    public void saveCommunicationMethod(@AuthenticationPrincipal CustomUserDetails principal,
                                        @RequestBody CommunicationMethodRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardingScheduleService.saveCommunicationMethod(userId,req);
    }

    @PostMapping("/daily-minute")
    public void saveDailyMinute (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody DailyMinuteRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardingScheduleService.saveDailyMinute(userId,req);
    }
    @PostMapping("/group-size")
    public void saveOnboardingGroupSize(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody GroupSizeRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardingScheduleService.saveOnboardingGroupSize(userId,req);
    }

    @PostMapping
    public void saveOnboardingSchedules(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody OnboardingScheduleRequests req
    ) {
        UUID userId = principal.getUuid();
        onboardingScheduleService.saveOnboardingSchedules(userId,req);

    }

    @GetMapping("/communication-methods")
    public List<CommunicationMethodResponse> getAllCommunication(
    ) {
        return onboardingScheduleService.getAllCommunication();
    }

    @GetMapping("/daily-methods")
    public List<DailyMinuteResponse> getAllDailyMethod(
    ) {
        return onboardingScheduleService.getAllDailyMethod();
    }

    @GetMapping("/group-sizes")
    public List<GroupSizeResponse> getAllGroupSize(
    ) {
        return onboardingScheduleService.getAllGroupSize();
    }

}
