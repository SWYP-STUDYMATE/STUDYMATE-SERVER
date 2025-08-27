# 🚨 백엔드 에러 핸들링 가이드

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Backend Team
- **목적**: STUDYMATE 백엔드 에러 처리 표준화 및 구현 가이드

---

## 🎯 에러 처리 원칙

### 기본 원칙
1. **일관된 응답 형식**: 모든 API에서 동일한 에러 응답 구조 사용
2. **의미있는 에러 코드**: 클라이언트가 적절히 처리할 수 있는 구체적 코드
3. **보안 고려**: 내부 시스템 정보 노출 방지
4. **로깅 전략**: 디버깅을 위한 충분한 로그 정보 수집

### 에러 응답 표준 형식
```json
{
  "success": false,
  "timestamp": "2025-08-27T10:30:00Z",
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다",
    "details": "요청된 사용자 ID가 존재하지 않습니다"
  },
  "path": "/api/v1/users/12345"
}
```

---

## 🏗️ 예외 클래스 계층 구조

### 커스텀 예외 클래스
```java
// 최상위 비즈니스 예외
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

// 도메인별 예외 클래스
public class UserException extends BusinessException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}

public class ChatException extends BusinessException {
    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}

public class OnboardingException extends BusinessException {
    public OnboardingException(ErrorCode errorCode) {
        super(errorCode);
    }
}

public class MatchingException extends BusinessException {
    public MatchingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
```

---

## 📋 에러 코드 정의

### ErrorCode 열거형
```java
@Getter
public enum ErrorCode {
    
    // 공통 에러 (1000~1999)
    INVALID_REQUEST(1000, "잘못된 요청입니다"),
    UNAUTHORIZED(1001, "인증이 필요합니다"),
    FORBIDDEN(1002, "접근 권한이 없습니다"),
    INTERNAL_SERVER_ERROR(1003, "서버 내부 오류가 발생했습니다"),
    
    // 인증 관련 에러 (2000~2999)
    INVALID_TOKEN(2000, "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(2001, "토큰이 만료되었습니다"),
    OAUTH_ERROR(2002, "OAuth 인증 중 오류가 발생했습니다"),
    LOGIN_FAILED(2003, "로그인에 실패했습니다"),
    
    // 사용자 관련 에러 (3000~3999)
    USER_NOT_FOUND(3000, "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(3001, "이미 존재하는 사용자입니다"),
    INVALID_USER_DATA(3002, "사용자 데이터가 유효하지 않습니다"),
    PROFILE_UPDATE_FAILED(3003, "프로필 업데이트에 실패했습니다"),
    
    // 채팅 관련 에러 (4000~4999)
    CHAT_ROOM_NOT_FOUND(4000, "채팅방을 찾을 수 없습니다"),
    CHAT_ROOM_FULL(4001, "채팅방 인원이 가득 찼습니다"),
    MESSAGE_SEND_FAILED(4002, "메시지 전송에 실패했습니다"),
    WEBSOCKET_CONNECTION_ERROR(4003, "WebSocket 연결 오류가 발생했습니다"),
    
    // 온보딩 관련 에러 (5000~5999)
    ONBOARDING_NOT_FOUND(5000, "온보딩 정보를 찾을 수 없습니다"),
    ONBOARDING_ALREADY_COMPLETED(5001, "이미 온보딩이 완료되었습니다"),
    INVALID_ONBOARDING_DATA(5002, "온보딩 데이터가 유효하지 않습니다"),
    
    // 매칭 관련 에러 (6000~6999)
    MATCHING_FAILED(6000, "매칭에 실패했습니다"),
    NO_AVAILABLE_MATCHES(6001, "매칭 가능한 상대가 없습니다"),
    MATCHING_ALREADY_EXISTS(6002, "이미 매칭이 진행 중입니다"),
    
    // 파일 관련 에러 (7000~7999)
    FILE_UPLOAD_FAILED(7000, "파일 업로드에 실패했습니다"),
    INVALID_FILE_FORMAT(7001, "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(7002, "파일 크기가 초과되었습니다"),
    
    // 외부 API 관련 에러 (8000~8999)
    EXTERNAL_API_ERROR(8000, "외부 API 호출 중 오류가 발생했습니다"),
    NAVER_API_ERROR(8001, "Naver API 오류"),
    CLOVA_API_ERROR(8002, "Clova API 오류");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

---

## 🎛️ 글로벌 예외 처리

### GlobalExceptionHandler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorDetail.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .details(ex.getMessage())
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Business Exception: {} - {}", errorCode.name(), ex.getMessage());
        
        return ResponseEntity.status(determineHttpStatus(errorCode)).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("입력 데이터 검증에 실패했습니다")
                .details(ex.getMessage())
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.warn("Validation Exception: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorDetail.builder()
                .code("DATA_INTEGRITY_ERROR")
                .message("데이터 무결성 제약 위반")
                .details("요청한 작업이 데이터 제약 조건을 위반합니다")
                .build())
            .path(request.getRequestURI())
            .build();
            
        log.error("Data Integrity Violation: {}", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(ErrorDetail.builder()
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
        if (code >= 1002) return HttpStatus.FORBIDDEN;
        
        return HttpStatus.BAD_REQUEST;
    }
}
```

---

## 📝 응답 DTO 클래스

### ErrorResponse
```java
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
```

### 성공 응답
```java
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
}
```

---

## 🎪 도메인별 에러 처리

### User 도메인 예외 처리
```java
@Service
@Slf4j
public class UserService {
    
    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
            
        return UserDto.from(user);
    }
    
    public UserDto updateProfile(Long userId, UserUpdateDto dto) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
                
            // 프로필 업데이트 로직
            user.updateProfile(dto);
            User savedUser = userRepository.save(user);
            
            return UserDto.from(savedUser);
            
        } catch (DataIntegrityViolationException ex) {
            log.error("Profile update failed for user {}: {}", userId, ex.getMessage());
            throw new UserException(ErrorCode.PROFILE_UPDATE_FAILED);
        }
    }
}
```

### Chat 도메인 예외 처리
```java
@Service
@Slf4j
public class ChatService {
    
    public ChatRoomDto createChatRoom(CreateChatRoomDto dto) {
        try {
            // 채팅방 생성 로직
            ChatRoom chatRoom = ChatRoom.create(dto);
            ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
            
            return ChatRoomDto.from(savedRoom);
            
        } catch (Exception ex) {
            log.error("Failed to create chat room: {}", ex.getMessage());
            throw new ChatException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDto message) {
        try {
            // 메시지 유효성 검증
            validateMessage(message);
            
            // 채팅방 존재 여부 확인
            ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new ChatException(ErrorCode.CHAT_ROOM_NOT_FOUND));
                
            // 메시지 저장 및 브로드캐스트
            ChatMessage savedMessage = chatMessageRepository.save(ChatMessage.from(message));
            messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(), 
                ChatMessageDto.from(savedMessage)
            );
            
        } catch (BusinessException ex) {
            // 비즈니스 예외는 그대로 전파
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to send message: {}", ex.getMessage());
            throw new ChatException(ErrorCode.MESSAGE_SEND_FAILED);
        }
    }
}
```

---

## 🔍 입력값 검증 및 에러 처리

### Validation 어노테이션
```java
@Getter
@Builder
public class UserUpdateDto {
    
    @NotBlank(message = "영어명은 필수입니다")
    @Size(max = 50, message = "영어명은 50자를 초과할 수 없습니다")
    private String englishName;
    
    @Size(max = 500, message = "자기소개는 500자를 초과할 수 없습니다")
    private String bio;
    
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$", message = "위치는 한글, 영문, 숫자만 포함할 수 있습니다")
    private String location;
    
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;
}
```

### 컨트롤러에서 검증 처리
```java
@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UserUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            UserDto updatedUser = userService.updateProfile(userPrincipal.getId(), dto);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "프로필이 업데이트되었습니다"));
            
        } catch (UserException ex) {
            // GlobalExceptionHandler에서 처리
            throw ex;
        }
    }
}
```

### 검증 에러 처리
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex, HttpServletRequest request) {
    
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
        .path(request.getRequestURI())
        .build();
    
    return ResponseEntity.badRequest().body(response);
}
```

---

## 📊 로깅 전략

### 로그 레벨 및 용도
```yaml
logging:
  level:
    com.studymate: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n'
    file: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n'
```

### 구조화된 로깅
```java
@Slf4j
public class UserService {
    
    public UserDto getUser(Long userId) {
        log.info("Getting user profile for userId: {}", userId);
        
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });
                
            log.debug("Successfully retrieved user: userId={}, username={}", 
                user.getId(), user.getEnglishName());
                
            return UserDto.from(user);
            
        } catch (Exception ex) {
            log.error("Error getting user: userId={}, error={}", userId, ex.getMessage(), ex);
            throw ex;
        }
    }
}
```

### MDC를 활용한 트레이스 ID
```java
@Component
public class TraceIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        
        try {
            MDC.put("traceId", traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

---

## 🚨 모니터링 및 알람

### 헬스 체크 엔드포인트
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Health health() {
        try {
            // 데이터베이스 연결 상태 확인
            long userCount = userRepository.count();
            
            return Health.up()
                .withDetail("database", "Available")
                .withDetail("userCount", userCount)
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 에러율 모니터링
```java
@Component
@Slf4j
public class ErrorMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter errorCounter;
    
    public ErrorMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.errorCounter = Counter.builder("application.errors")
            .description("Application error count")
            .register(meterRegistry);
    }
    
    @EventListener
    public void handleErrorEvent(ErrorEvent event) {
        errorCounter.increment(
            Tags.of(
                "error.code", event.getErrorCode(),
                "error.type", event.getErrorType()
            )
        );
        
        log.error("Error occurred: code={}, type={}, message={}", 
            event.getErrorCode(), event.getErrorType(), event.getMessage());
    }
}
```

---

## 📚 관련 문서

- [서비스 아키텍처](./services-overview.md)
- [API 레퍼런스](../04-api/api-reference.md)
- [데이터베이스 설계](../05-database/database-schema.md)
- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [배포 가이드](../08-infrastructure/deployment-guide.md)