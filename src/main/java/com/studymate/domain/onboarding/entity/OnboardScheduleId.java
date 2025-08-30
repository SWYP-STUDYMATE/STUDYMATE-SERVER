package com.studymate.domain.onboarding.entity;

import com.studymate.domain.onboarding.domain.type.DayOfWeekType;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OnboardScheduleId implements Serializable {
    private UUID userId;
    private int scheduleId;
    private String dayOfWeek;  // 요일 정보
    private LocalTime classTime;  // 수업 시간

    // 편의 생성자
    public OnboardScheduleId(UUID userId, int scheduleId) {
        this.userId = userId;
        this.scheduleId = scheduleId;
    }

    // DayOfWeekType을 받는 setter 추가
    public void setDayOfWeek(DayOfWeekType dayOfWeekType) {
        this.dayOfWeek = dayOfWeekType != null ? dayOfWeekType.name() : null;
    }
}
