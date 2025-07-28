package com.studymate.domain.onboarding.service;

import com.studymate.domain.onboarding.domain.dto.request.CommunicationMethodRequest;
import com.studymate.domain.onboarding.domain.dto.request.DailyMinuteRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequest;
import com.studymate.domain.onboarding.domain.dto.request.OnboardScheduleRequests;
import com.studymate.domain.onboarding.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboarding.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;

import java.util.List;
import java.util.UUID;

public interface OnboardScheduleService {
    void saveOnboardSchedules(UUID userId,OnboardScheduleRequests req);
    void saveDailyMinute(UUID userId,DailyMinuteRequest req);
    void saveCommunicationMethod(UUID userId,CommunicationMethodRequest req);
    List<CommunicationMethodResponse> getAllCommunication();
    List<DailyMinuteResponse> getAllDailyMethod();
}
