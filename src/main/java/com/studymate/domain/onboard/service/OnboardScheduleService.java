package com.studymate.domain.onboard.service;

import com.studymate.domain.onboard.domain.dto.request.*;
import com.studymate.domain.onboard.domain.dto.response.CommunicationMethodResponse;
import com.studymate.domain.onboard.domain.dto.response.DailyMinuteResponse;
import com.studymate.domain.onboard.domain.dto.response.GroupSizeResponse;

import java.util.List;
import java.util.UUID;

public interface OnboardScheduleService {
    void saveOnboardSchedules(UUID userId,OnboardScheduleRequests req);
    void saveDailyMinute(UUID userId,DailyMinuteRequest req);
    void saveCommunicationMethod(UUID userId,CommunicationMethodRequest req);
    void saveOnboardGroupSize(UUID userId, GroupSizeRequest req);
    List<CommunicationMethodResponse> getAllCommunication();
    List<DailyMinuteResponse> getAllDailyMethod();
    List<GroupSizeResponse> getAllGroupSize();
}
