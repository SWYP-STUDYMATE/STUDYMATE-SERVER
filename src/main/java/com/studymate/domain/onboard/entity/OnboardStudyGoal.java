package com.studymate.domain.onboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 온보딩 학습 목표 매핑 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARDING_STUDY_GOAL")
public class OnboardStudyGoal {

    @EmbeddedId
    private OnboardMotivationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOTIVATION_ID", insertable = false, updatable = false)
    private Motivation motivation;

    // 편의 메서드들
    public int getMotivationId() {
        return this.id.getMotivationId();
    }

    public String getGoalType() {
        return this.motivation != null ? this.motivation.getName() : null;
    }
}