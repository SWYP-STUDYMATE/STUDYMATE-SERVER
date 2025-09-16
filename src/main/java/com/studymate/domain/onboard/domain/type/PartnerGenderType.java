package com.studymate.domain.onboard.domain.type;

import lombok.Getter;

@Getter
public enum PartnerGenderType {
    FEMALE("여성"),
    MALE("남성"),
    ANY("상관없음");

    private final String description;

    PartnerGenderType(String description){
        this.description = description;
    }
}
