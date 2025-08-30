package com.studymate.domain.chat.exception;

import com.studymate.common.exception.BusinessException;
import com.studymate.common.exception.ErrorCode;

public class ChatException extends BusinessException {
    
    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ChatException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public ChatException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}