package com.studymate.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private LocalDateTime timestamp;
    private T data;
    private String message;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .timestamp(LocalDateTime.now())
            .data(data)
            .message("Success")
            .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .timestamp(LocalDateTime.now())
            .data(data)
            .message(message)
            .build();
    }
    
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
            .success(true)
            .timestamp(LocalDateTime.now())
            .message("Success")
            .build();
    }
    
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
            .success(true)
            .timestamp(LocalDateTime.now())
            .message(message)
            .build();
    }
}