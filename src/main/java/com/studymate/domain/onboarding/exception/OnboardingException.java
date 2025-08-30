package com.studymate.domain.onboarding.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class OnboardingException extends BusinessException {
    
    public OnboardingException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public OnboardingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public OnboardingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}