# 🚀 백엔드 서비스 아키텍처

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Backend Team
- **목적**: STUDYMATE 백엔드 서비스 구조 및 구현 가이드

---

## 🏗️ 전체 서비스 아키텍처

### 계층화 아키텍처 (Layered Architecture)
```
┌─────────────────────────────┐
│     Controller Layer        │  ← REST API 엔드포인트
│   (Presentation Layer)      │
├─────────────────────────────┤
│      Service Layer          │  ← 비즈니스 로직
│   (Business Logic Layer)    │
├─────────────────────────────┤
│    Repository Layer         │  ← 데이터 액세스
│   (Data Access Layer)       │
├─────────────────────────────┤
│     Infrastructure          │  ← 외부 시스템 연동
│   (External Services)       │
└─────────────────────────────┘
```

---

## 📦 도메인 서비스 구조

### 패키지 구조
```
com.studymate/
├── config/                    # 설정 및 Configuration
│   ├── SecurityConfig         # Spring Security 설정
│   ├── WebSocketConfig        # WebSocket 설정
│   ├── RedisConfig           # Redis 설정
│   └── SwaggerConfig         # API 문서 설정
├── common/                   # 공통 컴포넌트
│   ├── dto/                  # 공통 DTO
│   ├── entity/               # 공통 엔티티
│   └── exception/            # 공통 예외
├── auth/                     # 인증 서비스
│   ├── jwt/                  # JWT 처리
│   ├── oauth/                # OAuth 처리
│   └── filter/               # 인증 필터
└── domain/                   # 비즈니스 도메인
    ├── user/                 # 사용자 관리
    ├── chat/                 # 채팅 시스템
    ├── onboard/           # 온보딩 프로세스
    └── matching/             # 매칭 시스템
```

---

## 🎯 핵심 도메인 서비스

### 1. User 도메인 (사용자 관리)

#### 서비스 구조
```java
@Service
public class UserService {
    // 사용자 프로필 관리
    // Naver OAuth 연동
    // JWT 토큰 관리
}
```

#### 주요 기능
- **사용자 등록/수정**: Naver OAuth를 통한 회원가입
- **프로필 관리**: 위치, 영어명, 자기소개, 프로필 이미지
- **인증 처리**: JWT 토큰 발급 및 검증
- **권한 관리**: Spring Security 기반 접근 제어

#### API 엔드포인트
```
GET    /api/v1/users/profile     # 내 프로필 조회
PUT    /api/v1/users/profile     # 프로필 수정
POST   /api/v1/users/upload      # 프로필 이미지 업로드
DELETE /api/v1/users/profile     # 계정 탈퇴
```

### 2. Chat 도메인 (실시간 채팅)

#### 서비스 구조
```java
@Service
public class ChatService {
    // 채팅방 관리
    // 메시지 처리
    // WebSocket 세션 관리
}

@Controller
public class ChatController {
    // STOMP 메시지 처리
    // 실시간 메시지 브로드캐스트
}
```

#### 주요 기능
- **실시간 메시지**: STOMP over WebSocket
- **채팅방 관리**: 1:1 및 그룹 채팅방 생성/관리
- **메시지 저장**: 채팅 히스토리 데이터베이스 저장
- **온라인 상태**: 사용자 접속 상태 실시간 추적

#### WebSocket 구조
```
연결: /ws
구독: /topic/chat/{roomId}
발송: /app/chat/send
```

### 3. Onboard 도메인 (온보딩)

#### 서비스 구조
```java
@Service  
public class OnboardService {
    // 온보딩 설문 처리
    // 사용자 선호도 저장
    // 레벨 테스트 결과 처리
}
```

#### 주요 기능
- **언어 설정**: 학습 언어, 수준, 모국어
- **학습 스타일**: 동기, 학습 방법, 소통 선호도
- **파트너 선호도**: 성별, 나이대, 성격 유형
- **스케줄 관리**: 요일별 학습 시간 설정

#### 온보딩 플로우
```
1. 기본 정보 → 2. 언어 설정 → 3. 학습 스타일 → 4. 파트너 선호도 → 5. 스케줄 설정
```

### 4. Matching 도메인 (매칭 시스템)

#### 서비스 구조
```java
@Service
public class MatchingService {
    // 매칭 알고리즘
    // 호환성 계산
    // 매칭 결과 생성
}
```

#### 주요 기능
- **스마트 매칭**: 언어 수준, 선호도, 스케줄 기반
- **호환성 점수**: 다차원 매칭 알고리즘
- **매칭 히스토리**: 매칭 결과 및 피드백 추적
- **실시간 매칭**: WebSocket을 통한 즉시 매칭

---

## 🔧 인프라스트럭처 서비스

### 1. Authentication & Authorization

#### JWT 기반 인증
```java
@Component
public class JwtTokenProvider {
    // JWT 토큰 생성
    // 토큰 검증
    // 토큰에서 사용자 정보 추출
}

@Component  
public class JwtAuthenticationFilter {
    // HTTP 요청 인터셉트
    // 토큰 검증 후 SecurityContext 설정
}
```

#### OAuth 연동
```java
@FeignClient(name = "naver-api")
public interface NaverApi {
    // Naver OAuth API 호출
    // 사용자 프로필 정보 조회
}
```

### 2. 데이터 액세스 계층

#### JPA Repository 패턴
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNaverId(String naverId);
    List<User> findByLocationAndActiveTrue(String location);
}
```

#### Redis 캐싱 서비스
```java
@Service
public class RedisService {
    // 세션 정보 캐싱
    // 채팅 메시지 캐싱
    // 매칭 결과 임시 저장
}
```

### 3. 파일 저장 서비스

#### NCP Object Storage
```java
@Service
public class FileStorageService {
    // 프로필 이미지 업로드
    // 채팅 파일 저장
    // CDN을 통한 파일 서빙
}
```

---

## 🚦 서비스 간 통신

### 1. 동기 통신 패턴

#### Service Layer 호출
```java
@Service
public class UserService {
    
    @Autowired
    private MatchingService matchingService;
    
    public void updateProfile(UserProfileDto profile) {
        // 1. 프로필 업데이트
        userRepository.save(user);
        
        // 2. 매칭 정보 갱신
        matchingService.updateUserPreferences(user.getId());
    }
}
```

### 2. 비동기 통신 패턴

#### Event Driven Architecture
```java
@EventListener
public class UserEventHandler {
    
    @Async
    public void handleProfileUpdateEvent(UserProfileUpdatedEvent event) {
        // 비동기로 매칭 알고리즘 업데이트
        matchingService.recalculateMatches(event.getUserId());
    }
}
```

#### WebSocket 실시간 통신
```java
@MessageMapping("/chat/send")
public void sendMessage(ChatMessageDto message) {
    // 메시지 저장
    chatService.saveMessage(message);
    
    // 실시간 브로드캐스트
    messagingTemplate.convertAndSend(
        "/topic/chat/" + message.getRoomId(), 
        message
    );
}
```

---

## 📊 성능 최적화 전략

### 1. 데이터베이스 최적화

#### 쿼리 최적화
- **인덱스 활용**: 자주 조회되는 컬럼에 인덱스 설정
- **N+1 문제 해결**: `@EntityGraph` 또는 Fetch Join 사용
- **배치 처리**: 대량 데이터 처리 시 배치 사이즈 조정

#### 예시 코드
```java
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @EntityGraph(attributePaths = {"sender", "chatRoom"})
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long roomId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.createdAt > :since")
    List<ChatMessage> findRecentMessages(@Param("roomId") Long roomId, @Param("since") LocalDateTime since);
}
```

### 2. 캐싱 전략

#### Redis 캐시 레벨
```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#userId")
    public UserDto getUser(Long userId) {
        return userRepository.findById(userId)
            .map(UserDto::from)
            .orElseThrow();
    }
    
    @CacheEvict(value = "users", key = "#userId")
    public void updateUser(Long userId, UserUpdateDto dto) {
        // 캐시 무효화 후 업데이트
    }
}
```

### 3. 비동기 처리

#### 매칭 알고리즘 비동기화
```java
@Service
public class MatchingService {
    
    @Async("matchingExecutor")
    public CompletableFuture<List<MatchResult>> findMatches(Long userId) {
        // 복잡한 매칭 알고리즘을 비동기로 실행
        List<MatchResult> results = performMatching(userId);
        return CompletableFuture.completedFuture(results);
    }
}
```

---

## 🔒 보안 고려사항

### 1. 인증 및 권한

#### Spring Security 설정
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login/**", "/auth/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### 2. 데이터 보호

#### 민감 정보 처리
- **개인정보 암호화**: AES-256 암호화
- **패스워드 해싱**: BCrypt 사용
- **로그 필터링**: 민감 정보 로그 출력 방지

#### 예시 코드
```java
@Entity
public class User extends BaseTimeEntity {
    
    @Column(nullable = false)
    private String naverId;
    
    @Convert(converter = StringCryptoConverter.class)
    private String phoneNumber;  // 암호화 저장
    
    // 민감 정보는 JSON 직렬화에서 제외
    @JsonIgnore
    private String internalToken;
}
```

---

## 🚀 배포 및 운영

### 1. Docker 컨테이너 구성

#### Spring Boot 애플리케이션
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/studymate-server.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
```

### 2. 헬스 체크 및 모니터링

#### Actuator 엔드포인트
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### 커스텀 헬스 체크
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 데이터베이스 연결 상태 확인
            userRepository.count();
            return Health.up().withDetail("database", "Available").build();
        } catch (Exception e) {
            return Health.down().withDetail("database", e.getMessage()).build();
        }
    }
}
```

---

## 📚 관련 문서

- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [API 레퍼런스](../04-api/api-reference.md)
- [데이터베이스 설계](../05-database/database-schema.md)
- [에러 처리 가이드](./error-handling.md)
- [배포 가이드](../08-infrastructure/deployment-guide.md)