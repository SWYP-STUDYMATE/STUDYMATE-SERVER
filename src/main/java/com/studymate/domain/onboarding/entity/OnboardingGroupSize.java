package com.studymate.domain.onboarding.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARDING_GROUP_SIZE")
public class OnboardingGroupSize {

    @EmbeddedId
    private OnboardingGroupSizeId id;

    // 편의 메서드
    public int getGroupSizeId() {
        return this.id.getGroupSizeId();
    }
}
