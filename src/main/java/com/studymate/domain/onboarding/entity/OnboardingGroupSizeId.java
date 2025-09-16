package com.studymate.domain.onboarding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OnboardingGroupSizeId {
    @Column(name = "USER_ID")
    private UUID userId;
    
    @Column(name = "GROUP_SIZE_ID")
    private int groupSizeId;
}
