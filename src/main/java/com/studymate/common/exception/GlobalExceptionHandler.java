package com.studymate.common.exception;

import com.studymate.common.dto.ErrorResponse;
import com.studymate.common.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .details(ex.getMessage())
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Business Exception: {} - {}", errorCode.name(), ex.getMessage());
        
        return ResponseEntity.status(determineHttpStatus(errorCode)).body(response);
    }

    @ExceptionHandler(StudymateException.class)
    public ResponseEntity<Object> handleStudymateException(StudymateException exception, 
            HttpServletRequest request) {
        if (exception.isMessageNotEmpty()) {
            log.warn("[{}] {}", exception.getStudymateExceptionType().name(), exception.getMessage());
        }

        if (exception.isInternalServerError()) {
            log.error("Internal Server Error, type: {}", exception.getStudymateExceptionType(), exception);
        }

        int httpStatus = exception.getStudymateExceptionType().getHttpStatus().value();
        
        // 새로운 표준 형식으로 응답
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code(exception.getStudymateExceptionType().name())
                .message(exception.getStudymateExceptionType().getDefaultMessage())
                .details(exception.getMessage())
                .build())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(httpStatus).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code("INVALID_REQUEST")
                .message("잘못된 요청입니다")
                .details(ex.getMessage())
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Illegal Argument Exception: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code("DATA_INTEGRITY_ERROR")
                .message("데이터 무결성 제약 위반")
                .details("요청한 작업이 데이터 제약 조건을 위반합니다")
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.error("Data Integrity Violation: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .rejectedValue(error.getRejectedValue())
                .message(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("입력 데이터 검증에 실패했습니다")
                .fieldErrors(fieldErrors)
                .build())
            .path(((HttpServletRequest) request).getRequestURI())
            .build();
        
        log.warn("Validation Exception: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorResponse.ErrorDetail.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다")
                .details("일시적인 오류일 수 있습니다. 잠시 후 다시 시도해주세요")
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.error("Unexpected Exception: {}", ex.getMessage(), ex);
        
        return ResponseEntity.internalServerError().body(response);
    }
    
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        int code = errorCode.getCode();
        
        if (code >= 2000 && code < 3000) return HttpStatus.UNAUTHORIZED;
        if (code >= 3000 && code < 4000) return HttpStatus.NOT_FOUND;
        if (code == 1002) return HttpStatus.FORBIDDEN;
        if (code >= 1003) return HttpStatus.INTERNAL_SERVER_ERROR;
        
        return HttpStatus.BAD_REQUEST;
    }
}
