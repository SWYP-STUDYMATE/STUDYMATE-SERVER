package com.studymate.domain.onboard.domain.dto.request;

import com.studymate.domain.onboard.domain.type.DailyMinuteType;

import java.util.UUID;

public record DailyMinuteRequest (
        DailyMinuteType dailyMinutesType
) {
}
