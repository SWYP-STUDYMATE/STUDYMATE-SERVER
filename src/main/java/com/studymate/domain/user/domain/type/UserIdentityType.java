package com.studymate.domain.user.domain.type;

import lombok.Getter;

@Getter
public enum UserIdentityType {
    NAVER("NAVER"),
    GOOGLE("GOOGLE");

    private final String type;

    UserIdentityType(String type) {
        this.type = type;
    }

}
