package com.studymate.domain.onboard.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class OnboardBusinessException extends BusinessException {
    
    public OnboardBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public OnboardBusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public OnboardBusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // 편의 메서드 - 커스텀 에러를 위해
    public OnboardBusinessException(String message) {
        super(ErrorCode.INVALID_ONBOARDING_DATA, message);
    }
}