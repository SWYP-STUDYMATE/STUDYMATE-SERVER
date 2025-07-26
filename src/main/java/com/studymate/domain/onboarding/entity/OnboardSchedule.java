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
@Table(name = "ONBOARD_SCHEDULE")
public class OnboardSchedule {

    @Id
    @Column(name = "USER_ID")
    private UUID userId;

    @Column(name = "DAY_OF_WEEK")
    private DayOfWeekType dayOfWeek;

    @Column(name = "CLASS_TIME")
    private LocalTime classTime;








}
