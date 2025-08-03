package com.studymate.domain.user.domain.type;

import lombok.Getter;

@Getter
public enum UserGenderType {
    MALE("남성"),
    FEMALE("여성"),
    NONE("없음");

    private final String description;

    UserGenderType(String description) {this.description = description;}

}
