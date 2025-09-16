package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.*;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.dto.response.GroupSizeResponse;

import java.util.List;
import java.util.UUID;

public interface OnboardingScheduleService {
    void saveOnboardingSchedules(UUID userId,OnboardingScheduleRequests req);
    void saveDailyMinute(UUID userId,DailyMinuteRequest req);
    void saveCommunicationMethod(UUID userId,CommunicationMethodRequest req);
    void saveOnboardingGroupSize(UUID userId, GroupSizeRequest req);
    List<CommunicationMethodResponse> getAllCommunication();
    List<DailyMinuteResponse> getAllDailyMethod();
    List<GroupSizeResponse> getAllGroupSize();
}
