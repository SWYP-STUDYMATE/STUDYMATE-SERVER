package com.studymate.domain.onboarding.domain.dto.request;

import java.util.List;

public record OnboardingScheduleRequests(
        List<OnboardingScheduleRequest> schedules
) {
}
