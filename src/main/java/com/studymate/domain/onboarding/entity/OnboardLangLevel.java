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
@Table(name = "ONBOARD_LANG_LEVEL")
public class OnboardLangLevel {

    @EmbeddedId
    private OnboardLangLevelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LANG_LEVEL_TYPE")
    private LangLevelType langLevelType;


}
