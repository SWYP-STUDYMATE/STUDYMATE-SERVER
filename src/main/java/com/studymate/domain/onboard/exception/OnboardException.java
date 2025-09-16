package com.studymate.domain.onboard.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class OnboardException extends BusinessException {
    
    public OnboardException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public OnboardException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public OnboardException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}