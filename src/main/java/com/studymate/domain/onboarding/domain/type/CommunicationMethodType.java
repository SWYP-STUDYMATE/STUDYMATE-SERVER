package com.studymate.domain.onboarding.domain.type;

import lombok.Getter;

@Getter
public enum CommunicationMethodType {
    VIDEO("화상"),
    AUDIO_ONLY("오디오만"),
    HYBRID("하이브리드(화상과 오디오를 상황에 따라 병행");

    private final String description;

    CommunicationMethodType(String description){
        this.description = description;
    }
}
