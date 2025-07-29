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
    private DayOfWeekType dayOfWeek;
    private LocalTime classTime;

}
