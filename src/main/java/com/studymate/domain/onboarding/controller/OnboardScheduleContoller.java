package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.CommunicationMethodRequest;
import com.studymate.domain.onboarding.domain.dto.request.DailyMinuteRequest;
import com.studymate.domain.onboarding.domain.dto.request.GroupSizeRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequests;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.dto.response.GroupSizeResponse;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.service.OnboardScheduleService;
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
@RequestMapping("api/v1/onboard/schedule")
public class OnboardScheduleContoller {

    private final OnboardScheduleService onboardScheduleService;
    private final JwtUtils jwtUtils;

    @PostMapping("/communication-method")
    public void saveCommunicationMethod(@AuthenticationPrincipal CustomUserDetails principal,
                                        @RequestBody CommunicationMethodRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardScheduleService.saveCommunicationMethod(userId,req);
    }

    @PostMapping("/daily-minute")
    public void saveDailyMinute (@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestBody DailyMinuteRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardScheduleService.saveDailyMinute(userId,req);
    }
    @PostMapping("/group-size")
    public void saveOnboardGroupSize(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody GroupSizeRequest req
    ) {
        UUID userId = principal.getUuid();
        onboardScheduleService.saveOnboardGroupSize(userId,req);
    }

    @PostMapping
    public void saveOnboardSchedules(@AuthenticationPrincipal CustomUserDetails principal,
                                     @RequestBody OnboardScheduleRequests req
    ) {
        UUID userId = principal.getUuid();
        onboardScheduleService.saveOnboardSchedules(userId,req);

    }

    @GetMapping("/communication-methods")
    public List<CommunicationMethodResponse> getAllCommunication(
    ) {
        return onboardScheduleService.getAllCommunication();
    }

    @GetMapping("/daily-methods")
    public List<DailyMinuteResponse> getAllDailyMethod(
    ) {
        return onboardScheduleService.getAllDailyMethod();
    }

    @GetMapping("/group-sizes")
    public List<GroupSizeResponse> getAllGroupSize(
    ) {
        return onboardScheduleService.getAllGroupSize();
    }

}
