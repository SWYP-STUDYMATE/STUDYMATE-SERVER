package com.studymate.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success;
    private LocalDateTime timestamp;
    private ErrorDetail error;
    private String path;
    
    @Getter
    @Builder
    public static class ErrorDetail {
        private String code;
        private String message;
        private String details;
        private List<FieldError> fieldErrors;
    }
    
    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}