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



}
