package com.studymate.domain.onboarding.domain.dto.request;

import com.studymate.domain.onboarding.domain.type.DayOfWeekType;

import java.time.LocalTime;
import java.util.UUID;

public record OnboardScheduleRequest(
        DayOfWeekType dayOfWeek,
        LocalTime classTime
) {
}
