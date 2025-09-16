# 🔗 클라이언트-서버 통합 가이드

## 📅 문서 정보
- **버전**: 1.0
- **최종 업데이트**: 2025-09-10
- **작성자**: Backend Development Team
- **목적**: STUDYMATE 클라이언트-서버 간 데이터 정합성 및 통합 문서

---

## 🎯 통합 원칙

### 필수 상호 참조 체크리스트
- [ ] **TypeScript 인터페이스 동기화**: `/STYDYMATE-CLIENT/src/types/api.d.ts`
- [ ] **API 응답 형식 일치**: `ApiResponse<T>` 래퍼 구조
- [ ] **에러 코드 표준화**: 클라이언트-서버 공통 에러 코드
- [ ] **WebSocket 이벤트 매핑**: STOMP 메시지 형식 일치
- [ ] **상태 관리 동기화**: Zustand store와 서버 상태 일치

---

## 📊 데이터 타입 매핑

### 1. 공통 응답 구조

#### 서버 (Java/Spring)
```java
@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorResponse error;
}
```

#### 클라이언트 (TypeScript)
```typescript
export interface ApiResponse<T = any> {
    success: boolean;
    data?: T;
    message?: string;
    error?: {
        code: string;
        message: string;
    };
}
```

✅ **정합성 상태**: 완벽 일치

---

### 2. User 도메인 매핑

#### User 엔티티 → UserProfileResponse

| 서버 (Java) | 클라이언트 (TypeScript) | 타입 변환 | 정합성 |
|------------|-------------------------|----------|---------|
| UUID userId | string id | UUID→string | ✅ |
| String englishName | string englishName | 직접 매핑 | ✅ |
| String profileImage | string profileImageUrl | 필드명 차이 | ⚠️ |
| String selfBio | string selfBio | 직접 매핑 | ✅ |
| Location location | LocationResponse location | 중첩 객체 | ✅ |
| Language nativeLanguage | LanguageResponse nativeLanguage | 중첩 객체 | ✅ |
| String birthyear | number birthYear | String→number | ⚠️ |
| String birthday | string birthday | 직접 매핑 | ✅ |
| UserGenderType gender | UserGenderTypeResponse gender | Enum→객체 | ✅ |

#### 주의사항
- `profileImage` → `profileImageUrl` 필드명 차이
- `birthyear` String → number 타입 변환 필요

---

### 3. Location 도메인 매핑

#### 서버 Location 엔티티
```java
@Entity
public class Location {
    private int locationId;
    private String country;
    private String city;
    private String timeZone;
}
```

#### 클라이언트 LocationResponse
```typescript
export interface LocationResponse {
    id: number;
    name: string;
    country: string;
    countryCode?: string;
}
```

⚠️ **불일치 발견**:
- 서버: `locationId`, `city`, `timeZone`
- 클라이언트: `id`, `name`, `countryCode`
- **해결 필요**: DTO 변환 레이어에서 매핑 처리

---

### 4. Onboard 도메인 매핑

#### 언어 레벨 설정

| 서버 구조 | 클라이언트 요청 | 변환 필요 |
|----------|----------------|----------|
| OnboardLangLevel (복합키) | LanguageLevelRequest | ✅ |
| userId + languageId | languages[] 배열 | ✅ |
| currentLevel (엔티티) | currentLevelId (숫자) | ✅ |
| targetLevel (엔티티) | targetLevelId (숫자) | ✅ |

#### 온보딩 진행 상태
```typescript
// 클라이언트 기대 응답
export interface OnboardProgressResponse {
    step: number;
    completed: boolean;
    totalSteps: number;
}
```

서버에서 제공 필요:
- 현재 스텝 추적
- 완료된 스텝 목록
- 전체 스텝 수 (고정값)

---

## 🔄 API 엔드포인트 매핑

### User Controller

| 엔드포인트 | 메서드 | 요청 타입 | 응답 타입 | 상태 |
|-----------|--------|----------|----------|------|
| `/api/v1/users/profile` | GET | - | ApiResponse<UserProfileResponse> | ✅ |
| `/api/v1/users/english-name` | PUT | EnglishNameRequest | ApiResponse<UserNameResponse> | ✅ |
| `/api/v1/users/location` | PUT | LocationRequest | ApiResponse<LocationResponse> | ⚠️ |
| `/api/v1/users/gender` | PUT | UserGenderTypeRequest | ApiResponse<UserGenderTypeResponse> | ✅ |
| `/api/v1/users/bio` | PUT | SelfBioRequest | ApiResponse<void> | ✅ |

### Onboard Controller

| 엔드포인트 | 메서드 | 요청 타입 | 응답 타입 | 상태 |
|-----------|--------|----------|----------|------|
| `/api/v1/onboard/progress` | GET | - | ApiResponse<OnboardProgressResponse> | ✅ |
| `/api/v1/onboard/native-language` | POST | NativeLanguageRequest | ApiResponse<LanguageResponse> | ✅ |
| `/api/v1/onboard/language-levels` | POST | LanguageLevelRequest | ApiResponse<void> | ✅ |
| `/api/v1/onboard/complete` | POST | CompleteOnboardRequest | ApiResponse<OnboardStatusResponse> | ✅ |

---

## 🔌 WebSocket/STOMP 통합

### 연결 설정

#### 서버 설정
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

#### 클라이언트 연결 (예상)
```typescript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    // 채팅방 구독
    stompClient.subscribe('/topic/chat/' + roomId, (message) => {
        const chatMessage = JSON.parse(message.body);
        handleNewMessage(chatMessage);
    });
});
```

### 메시지 형식

#### 채팅 메시지 전송
```typescript
// 클라이언트 → 서버
interface ChatMessageRequest {
    roomId: number;
    content: string;
    messageType: 'TEXT' | 'IMAGE' | 'FILE';
}

// 서버 → 클라이언트
interface ChatMessageResponse {
    id: number;
    roomId: number;
    senderId: string;
    senderName: string;
    content: string;
    messageType: string;
    createdAt: string;
}
```

---

## 🚨 에러 코드 표준화

### 공통 에러 코드

| 코드 | HTTP 상태 | 설명 | 클라이언트 처리 |
|------|-----------|------|----------------|
| `AUTH_001` | 401 | 인증 토큰 없음 | 로그인 페이지로 리다이렉트 |
| `AUTH_002` | 401 | 토큰 만료 | 토큰 갱신 시도 |
| `AUTH_003` | 403 | 권한 없음 | 에러 메시지 표시 |
| `USER_001` | 404 | 사용자 없음 | 사용자 없음 안내 |
| `USER_002` | 409 | 중복 이메일 | 중복 안내 메시지 |
| `ONBOARD_001` | 400 | 온보딩 미완료 | 온보딩 페이지로 이동 |
| `CHAT_001` | 404 | 채팅방 없음 | 채팅방 목록으로 이동 |
| `CHAT_002` | 403 | 채팅방 접근 불가 | 접근 권한 없음 안내 |

### 에러 처리 통합

#### 서버 전역 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }
}
```

#### 클라이언트 에러 처리 (권장)
```typescript
// utils/errorHandler.ts
export const handleApiError = (error: ApiResponse['error']) => {
    switch (error?.code) {
        case 'AUTH_001':
        case 'AUTH_002':
            router.push('/login');
            break;
        case 'ONBOARD_001':
            router.push('/onboard');
            break;
        default:
            toast.error(error?.message || '오류가 발생했습니다');
    }
};
```

---

## 📱 상태 관리 동기화

### Zustand Store 구조 (클라이언트)

```typescript
// stores/userStore.ts
interface UserStore {
    user: UserProfileResponse | null;
    isOnboardCompleted: boolean;
    updateProfile: (data: Partial<UserProfileResponse>) => void;
    setOnboardStatus: (completed: boolean) => void;
}
```

### 서버 상태와 동기화 포인트

1. **로그인 시**: 전체 사용자 프로필 로드
2. **온보딩 완료 시**: `isOnboardCompleted` 업데이트
3. **프로필 수정 시**: 부분 업데이트 후 전체 리로드
4. **로그아웃 시**: 스토어 초기화

---

## 🔍 정합성 검증 체크리스트

### 개발 시 필수 확인 사항

#### 백엔드 개발자
- [ ] DTO 응답이 TypeScript 인터페이스와 일치하는가?
- [ ] 새로운 에러 코드를 클라이언트에 전달했는가?
- [ ] WebSocket 이벤트 형식을 문서화했는가?
- [ ] API 변경사항을 Swagger에 반영했는가?

#### 프론트엔드 개발자
- [ ] API 응답 타입이 서버 DTO와 일치하는가?
- [ ] 에러 처리가 서버 에러 코드와 매핑되는가?
- [ ] WebSocket 메시지 형식이 서버와 일치하는가?
- [ ] 상태 관리가 서버 상태와 동기화되는가?

---

## 🚀 개선 필요 사항

### 즉시 수정 필요
1. **Location 매핑 불일치**
   - 서버 DTO 수정 또는 클라이언트 인터페이스 업데이트
   
2. **birthyear 타입 불일치**
   - String → number 변환 로직 추가

3. **profileImage 필드명 불일치**
   - DTO에서 `profileImageUrl`로 통일

### 중기 개선 사항
1. **WebSocket 타입 정의 파일 생성**
   - 클라이언트에 WebSocket 메시지 타입 추가

2. **에러 코드 공유 모듈**
   - 클라이언트-서버 공통 에러 코드 패키지

3. **API 스펙 자동 생성**
   - OpenAPI 스펙에서 TypeScript 타입 자동 생성

---

## 📝 유지보수 가이드

### 변경 시 업데이트 필요 문서
1. **서버 DTO 변경**: 
   - `/STYDYMATE-CLIENT/src/types/api.d.ts`
   - 이 문서의 데이터 타입 매핑 섹션

2. **API 엔드포인트 변경**:
   - Swagger 문서
   - 이 문서의 API 엔드포인트 매핑 섹션

3. **에러 코드 추가**:
   - 이 문서의 에러 코드 표준화 섹션
   - 클라이언트 에러 핸들러

4. **WebSocket 이벤트 변경**:
   - 이 문서의 WebSocket/STOMP 통합 섹션
   - 클라이언트 WebSocket 핸들러

---

*이 문서는 클라이언트-서버 간 원활한 통합을 위한 가이드입니다.*
*변경사항은 반드시 양쪽 팀과 공유해주세요.*