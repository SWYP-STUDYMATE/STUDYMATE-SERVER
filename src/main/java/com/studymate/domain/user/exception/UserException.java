package com.studymate.domain.user.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class UserException extends BusinessException {
    
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public UserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public UserException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}