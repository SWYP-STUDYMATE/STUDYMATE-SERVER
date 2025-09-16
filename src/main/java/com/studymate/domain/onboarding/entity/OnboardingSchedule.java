package com.studymate.domain.onboarding.entity;

import com.studymate.domain.onboarding.domain.type.DayOfWeekType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARDING_SCHEDULE")
public class OnboardingSchedule {

    @EmbeddedId
    private OnboardingScheduleId id;

    // 편의 메서드
    public int getScheduleId() {
        return this.id.getScheduleId();
    }
}
