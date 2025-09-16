package com.studymate.domain.onboarding.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class OnboardingBusinessException extends BusinessException {
    
    public OnboardingBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public OnboardingBusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public OnboardingBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // 편의 메서드 - 커스텀 에러를 위해
    public OnboardingBusinessException(String message) {
        super(ErrorCode.INVALID_ONBOARDING_DATA, message);
    }
}