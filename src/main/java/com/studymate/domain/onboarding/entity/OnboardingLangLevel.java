package com.studymate.domain.onboarding.entity;

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
@Table(name = "ONBOARDING_LANG_LEVEL")
public class OnboardingLangLevel {

    @EmbeddedId
    private OnboardingLangLevelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANG_LEVEL_TYPE")
    private LangLevelType langLevelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANG_ID", insertable = false, updatable = false)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENT_LEVEL_ID")
    private LangLevelType currentLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_LEVEL_ID")
    private LangLevelType targetLevel;

    // 편의 메서드들
    public int getLanguageId() {
        return this.id.getLanguageId();
    }
}
