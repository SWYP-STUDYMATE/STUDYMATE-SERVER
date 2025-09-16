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
@Table(name = "ONBOARDING_LEARNING_STYLE")
public class OnboardingLearningStyle {

    @EmbeddedId
    private OnboardingLearningStyleId id;

    // 편의 메서드
    public int getLearningStyleId() {
        return this.id.getLearningStyleId();
    }
}
