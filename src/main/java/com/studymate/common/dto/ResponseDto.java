package com.studymate.common.dto;

import lombok.Getter;

@Getter
public class ResponseDto<T> {
    private final T data;
    private final String message;

    private ResponseDto(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> ResponseDto<T> of(T data, String message) {
        return new ResponseDto<>(data, message);
    }
}
