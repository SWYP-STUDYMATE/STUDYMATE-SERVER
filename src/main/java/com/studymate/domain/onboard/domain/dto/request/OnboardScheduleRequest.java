package com.studymate.domain.onboard.domain.dto.request;

import com.studymate.domain.onboard.domain.type.DayOfWeekType;

import java.time.LocalTime;
import java.util.UUID;

public record OnboardScheduleRequest(
        DayOfWeekType dayOfWeek,
        LocalTime classTime
) {
}
