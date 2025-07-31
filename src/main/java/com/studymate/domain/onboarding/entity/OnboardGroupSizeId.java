package com.studymate.domain.onboarding.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OnboardGroupSizeId {
    private UUID userid;
    private int groupSizeId;

}
