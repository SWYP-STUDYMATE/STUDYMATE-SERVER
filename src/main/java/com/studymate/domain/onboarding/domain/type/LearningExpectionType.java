package com.studymate.domain.onboarding.domain.type;

import lombok.Getter;

@Getter
public enum LearningExpectionType {
    HABIT("매일 조금씩이라도 꾸준히 이어갈 수 있는 습관"),
    CONFIDENCE("당당하게 대화할 수 있는 자신감"),
    CUSTOMIZED_METHOD("나에게 맞는 속도와 방식"),
    PRACTICAL_CONVERSATION("실전 상황에서도 바로 쓸 수 있는 회화력");

    private final String description;

    LearningExpectionType(String description) {
        this.description = description;
    }
}

