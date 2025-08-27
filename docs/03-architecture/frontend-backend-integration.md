# 🔗 STUDYMATE 프론트엔드-백엔드 연동 가이드

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: System Architecture Team
- **목적**: 프론트엔드와 백엔드 간 통신 방법 및 데이터 흐름 가이드

---

## 🏗️ 전체 시스템 구조

```
[Client Browser] ←→ [Cloudflare Pages] ←→ [Spring Boot API] ←→ [MySQL + Redis]
                           ↓
                    [Cloudflare Workers]
                      (AI/WebRTC 보조)
```

### 주요 구성 요소

#### 프론트엔드 (STUDYMATE-CLIENT)
- **Framework**: React 19.1.0 + Vite
- **Deployment**: Cloudflare Pages (`languagemate.kr`)
- **State Management**: Zustand
- **HTTP Client**: Axios with JWT interceptors
- **WebRTC**: Native WebRTC API + Cloudflare Workers

#### 백엔드 (STUDYMATE-SERVER)  
- **Framework**: Spring Boot 3.5.3 + Java 17
- **Deployment**: Docker Compose on NCP
- **Database**: MySQL 8.0 + Redis 7
- **Authentication**: JWT + Spring Security
- **API Docs**: Swagger/OpenAPI 3

---

## 🌐 API 통신 구조

### 1. API 베이스 URL 설정

**프론트엔드 설정** (`/STUDYMATE-CLIENT/src/api/index.js`)
```javascript
const api = axios.create({
  // 프로덕션: 리버스 프록시 via Cloudflare
  // 개발: Vite proxy 설정
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
});
```

**환경별 URL**
- **개발**: `http://localhost:8080/api/v1`
- **프로덕션**: `https://api.languagemate.kr/api/v1`

### 2. 인증 흐름

#### JWT 토큰 기반 인증
```javascript
// 요청 인터셉터: JWT 자동 첨부
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }
  return config;
});
```

#### 토큰 갱신 처리
```javascript
// 응답 인터셉터: 토큰 만료 시 자동 갱신
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !originalRequest._retry) {
      // refreshToken으로 새 accessToken 발급
      const refreshToken = localStorage.getItem("refreshToken");
      const res = await api.post("/auth/refresh", null, {
        headers: { Authorization: `Bearer ${refreshToken}` }
      });
      
      const { accessToken, refreshToken: newRefreshToken } = res.data;
      localStorage.setItem("accessToken", accessToken);
      if (newRefreshToken) {
        localStorage.setItem("refreshToken", newRefreshToken);
      }
      
      // 원래 요청 재시도
      return api(originalRequest);
    }
  }
);
```

---

## 🔐 OAuth 소셜 로그인 플로우

### Naver OAuth 
```
1. 프론트엔드: Naver 로그인 버튼 클릭
2. 리다이렉트: https://nid.naver.com/oauth2.0/authorize
3. 콜백: /login/oauth2/code/naver (Spring Security 처리)
4. 백엔드: 사용자 정보 조회 + JWT 생성
5. 프론트엔드: JWT 저장 + 온보딩/메인 페이지 이동
```

**프론트엔드 구현** (`/STUDYMATE-CLIENT/src/pages/Login/Login.jsx`)
```javascript
const handleNaverLogin = () => {
  const naverAuthURL = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
  window.location.href = naverAuthURL;
};
```

**백엔드 구현** (`/STUDYMATE-SERVER/src/main/java/com/studymate/auth/`)
- `NaverLoginController`: OAuth 콜백 처리
- `NaverApi`: Naver API 호출 (OpenFeign)
- `TokenService`: JWT 생성/검증

---

## 📊 주요 API 엔드포인트

### 1. 사용자 관리
| Method | Endpoint | 설명 | 프론트엔드 사용처 |
|--------|----------|------|-----------------|
| POST | `/api/v1/auth/refresh` | JWT 토큰 갱신 | `src/api/index.js` (interceptor) |
| GET | `/api/v1/user/name` | 사용자 이름 조회 | `src/api/index.js` |
| GET | `/api/v1/user/profile` | 사용자 프로필 조회 | `src/pages/Profile/ProfilePage.jsx` |
| PUT | `/api/v1/user/profile` | 프로필 업데이트 | `src/components/ProfileImageUpload.jsx` |

### 2. 온보딩
| Method | Endpoint | 설명 | 프론트엔드 사용처 |
|--------|----------|------|-----------------|
| POST | `/api/v1/onboarding/language` | 언어 설정 저장 | `src/pages/ObLang/` |
| POST | `/api/v1/onboarding/interests` | 관심사 저장 | `src/pages/ObInt/` |
| POST | `/api/v1/onboarding/partner` | 파트너 선호도 저장 | `src/pages/ObPartner/` |
| POST | `/api/v1/onboarding/schedule` | 스케줄 저장 | `src/pages/ObSchadule/` |

### 3. 레벨 테스트
| Method | Endpoint | 설명 | 프론트엔드 사용처 |
|--------|----------|------|-----------------|
| POST | `/api/v1/level-test/start` | 레벨 테스트 시작 | `src/pages/LevelTest/LevelTestStart.jsx` |
| POST | `/api/v1/level-test/submit` | 음성 답변 제출 | `src/pages/LevelTest/LevelTestRecording.jsx` |
| GET | `/api/v1/level-test/result` | 결과 조회 | `src/pages/LevelTest/LevelTestResult.jsx` |

### 4. 채팅 (WebSocket)
| Protocol | Endpoint | 설명 | 프론트엔드 사용처 |
|----------|----------|------|-----------------|
| WS | `/ws` | WebSocket 연결 | `src/components/chat/ChatContainer.jsx` |
| STOMP | `/app/chat.sendMessage` | 메시지 전송 | `src/components/chat/ChatInputArea.jsx` |
| STOMP | `/topic/chatroom/{roomId}` | 메시지 구독 | `src/components/chat/ChatMessageList.jsx` |

---

## 🎥 실시간 세션 (WebRTC + WebSocket)

### 하이브리드 아키텍처
```
[React WebRTC] ←→ [Cloudflare Workers] ←→ [Spring Boot WebSocket]
     ↓                    ↓                         ↓
[Media Stream]    [Signaling Server]        [Session Management]
```

### 1. WebRTC 시그널링 (Cloudflare Workers)
```javascript
// 프론트엔드: WebRTC 연결 설정
const createRoom = async () => {
  const response = await fetch('/api/v1/room/create', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const { roomId } = await response.json();
  return roomId;
};
```

### 2. 세션 관리 (Spring Boot)
```java
// 백엔드: 세션 생성/관리
@PostMapping("/api/v1/session/create")
public ResponseEntity<SessionDto> createSession(@RequestBody CreateSessionRequest request) {
    // 세션 생성 로직
    return ResponseEntity.ok(sessionDto);
}
```

---

## 📁 파일 업로드 시스템

### 프로필 이미지 업로드
```javascript
// 프론트엔드: 파일 업로드
const uploadProfileImage = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await api.post('/api/v1/upload/profile', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  
  return response.data.imageUrl;
};
```

```java
// 백엔드: NCP Object Storage 연동
@PostMapping("/api/v1/upload/profile")
public ResponseEntity<UploadResponse> uploadProfile(@RequestParam("file") MultipartFile file) {
    String imageUrl = ncpStorageService.upload(file);
    return ResponseEntity.ok(new UploadResponse(imageUrl));
}
```

---

## 🔄 데이터 상태 관리

### Zustand Store 구조
```javascript
// 사용자 프로필 스토어
const useProfileStore = create((set) => ({
  profile: null,
  setProfile: (profile) => set({ profile }),
  updateProfile: async (updates) => {
    const response = await api.put('/api/v1/user/profile', updates);
    set({ profile: response.data });
  }
}));
```

### Spring Boot Entity 구조
```java
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String name;
    private String profileImageUrl;
    
    // 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OnboardingLanguage> languages;
}
```

---

## 🚨 에러 처리

### 프론트엔드 에러 핸들링
```javascript
// 공통 에러 처리
const handleApiError = (error) => {
  if (error.response?.status === 401) {
    // 토큰 만료 -> 자동 재시도 (interceptor)
  } else if (error.response?.status === 403) {
    // 권한 없음 -> 로그인 페이지로 이동
    alert("접근 권한이 없습니다. 다시 로그인해주세요.");
    window.location.href = "/";
  } else {
    // 기타 에러
    console.error("API Error:", error.response?.data || error.message);
  }
};
```

### 백엔드 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(401)
            .body(new ErrorResponse("UNAUTHORIZED", e.getMessage()));
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(403)
            .body(new ErrorResponse("FORBIDDEN", e.getMessage()));
    }
}
```

---

## 🌍 개발 환경 설정

### 프론트엔드 환경 변수
```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
VITE_NAVER_CLIENT_ID=your-naver-client-id
```

### 백엔드 CORS 설정
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "https://languagemate.kr")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
```

### Vite 개발 서버 프록시
```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      }
    }
  }
});
```

---

## 🔍 디버깅 가이드

### API 호출 로그 확인
```javascript
// 프론트엔드: 네트워크 탭 또는 axios 로깅
api.interceptors.request.use((config) => {
  console.log('API Request:', config.method.toUpperCase(), config.url, config.data);
  return config;
});
```

### 백엔드 로그 설정
```yaml
# application.yml
logging:
  level:
    com.studymate: DEBUG
    org.springframework.web: DEBUG
```

### 일반적인 문제 해결

#### 1. CORS 에러
- **원인**: 프론트엔드와 백엔드 도메인이 다른 경우
- **해결**: 백엔드 CORS 설정에서 프론트엔드 도메인 추가

#### 2. 인증 실패
- **원인**: JWT 토큰 만료 또는 잘못된 토큰
- **해결**: 토큰 갱신 로직 확인, localStorage 토큰 상태 점검

#### 3. WebSocket 연결 실패  
- **원인**: 프록시 설정 또는 방화벽 문제
- **해결**: WebSocket 엔드포인트 및 네트워크 설정 확인

---

## 📈 성능 최적화

### 프론트엔드 최적화
- **Code Splitting**: React.lazy() + Suspense
- **Image Optimization**: `OptimizedImage` 컴포넌트 사용
- **API Caching**: React Query 또는 SWR 고려
- **Bundle Analysis**: `npm run analyze`

### 백엔드 최적화
- **Database Indexing**: 자주 조회되는 컬럼에 인덱스 추가
- **Redis Caching**: 세션 데이터 및 자주 조회되는 데이터 캐싱
- **Connection Pooling**: HikariCP 설정 최적화

---

## 🚀 배포 전 체크리스트

### 프론트엔드
- [ ] 환경 변수 설정 (`VITE_*`)
- [ ] API 엔드포인트 URL 확인
- [ ] OAuth 클라이언트 ID 설정
- [ ] Build 성공 확인 (`npm run build`)

### 백엔드
- [ ] 데이터베이스 연결 확인
- [ ] Redis 연결 확인
- [ ] JWT 시크릿 키 설정
- [ ] NCP Object Storage 설정
- [ ] CORS 도메인 설정

### 통합 테스트
- [ ] 로그인 플로우 테스트
- [ ] API 호출 테스트
- [ ] WebSocket 연결 테스트
- [ ] 파일 업로드 테스트
- [ ] 에러 처리 테스트