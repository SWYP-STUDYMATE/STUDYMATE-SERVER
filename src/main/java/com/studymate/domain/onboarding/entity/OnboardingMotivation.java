package com.studymate.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARDING_MOTIVATION")
public class OnboardingMotivation {

    @EmbeddedId
    private OnboardingMotivationId id;

    // 편의 메서드
    public int getMotivationId() {
        return this.id.getMotivationId();
    }
}
