package com.studymate.domain.matching.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class MatchingException extends BusinessException {
    
    public MatchingException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public MatchingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public MatchingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}