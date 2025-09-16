package com.studymate.domain.onboard.entity;

import com.studymate.domain.onboard.domain.type.DayOfWeekType;
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
@Table(name = "ONBOARD_SCHEDULE")
public class OnboardSchedule {

    @EmbeddedId
    private OnboardScheduleId id;

    // 편의 메서드
    public int getScheduleId() {
        return this.id.getScheduleId();
    }
}
