package com.studymate.domain.onboarding.controller;

import com.studymate.domain.onboarding.domain.dto.request.CommunicationMethodRequest;
import com.studymate.domain.onboarding.domain.dto.request.DailyMinuteRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequests;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.service.OnboardScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboard/schedule")
public class OnboardScheduleContoller {

    private final OnboardScheduleService onboardScheduleService;

    @PostMapping("/communication-method")
    public void saveCommunicationMethod(@RequestBody CommunicationMethodRequest req) {
        onboardScheduleService.saveCommunicationMethod(req);
    }

    @PostMapping("/daily-minute")
    public void saveDailyMinute (@RequestBody DailyMinuteRequest req) {
        onboardScheduleService.saveDailyMinute(req);
    }

    @PostMapping
    public void saveOnboardSchedules(@RequestBody OnboardScheduleRequests req) {
        onboardScheduleService.saveOnboardSchedules(req);

    }

    @GetMapping("/communication-methods")
    public List<CommunicationMethodResponse> getAllCommunication() {
        return onboardScheduleService.getAllCommunication();
    }

    @GetMapping("/daily-methods")
    public List<DailyMinuteResponse> getAllDailyMethod() {
        return onboardScheduleService.getAllDailyMethod();
    }

}
