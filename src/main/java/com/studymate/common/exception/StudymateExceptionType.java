package com.studymate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudymateExceptionType {
    // Success(200)
    OK(HttpStatus.OK, "Success"),

    // BAD_REQUEST(400)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청"),

    // UNAUTHORIZED(401)
    UNAUTHORIZED_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰"),

    // NOT_FOUND(404)
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NOT_FOUND_CHAT_ROOM(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // SERVER_ERROR(500)
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    public StudymateException of() {
        return new StudymateException(this);
    }

    public StudymateException of(String message) {
        return new StudymateException(this, message);
    }
}
