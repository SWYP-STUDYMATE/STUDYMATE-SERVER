package com.studymate.domain.onboarding.entity;

import com.studymate.domain.onboarding.domain.type.DayOfWeekType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "SCHEDULE")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_ID")
    private  int scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "DAY_OF_WEEK")
    private DayOfWeekType dayOfWeekType;

    @Column(name = "SCHEDULE_NAME")
    private String scheduleName;

    @Column(name = "TIME_SLOT")
    private String timeSlot;

    // 편의 메서드들
    public String getName() {
        return this.scheduleName;
    }

    public String getTimeSlot() {
        return this.timeSlot;
    }

    public String getDayOfWeek() {
        return this.dayOfWeekType != null ? this.dayOfWeekType.name() : null;
    }
}
