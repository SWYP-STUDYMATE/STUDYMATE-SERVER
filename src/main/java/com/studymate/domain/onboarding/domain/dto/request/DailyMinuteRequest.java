package com.studymate.domain.onboarding.domain.dto.request;

import com.studymate.domain.onboarding.domain.type.DailyMinuteType;

import java.util.UUID;

public record DailyMinuteRequest (
        UUID userId,
        DailyMinuteType dailyMinutesType
) {
}
