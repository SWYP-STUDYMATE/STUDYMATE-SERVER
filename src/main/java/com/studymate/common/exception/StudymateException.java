package com.studymate.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StudymateException extends RuntimeException {
    private final StudymateExceptionType studymateExceptionType;
    private final String message;

    public StudymateException(StudymateExceptionType studymateExceptionType) {
        this.studymateExceptionType = studymateExceptionType;
        this.message = studymateExceptionType.getDefaultMessage();
    }

    public StudymateException(StudymateExceptionType studymateExceptionType, String message) {
        this.studymateExceptionType = studymateExceptionType;
        this.message = message;
    }

    public boolean isMessageNotEmpty() {
        return this.message != null && !this.message.isEmpty();
    }

    public boolean isInternalServerError() {
        return this.studymateExceptionType.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
