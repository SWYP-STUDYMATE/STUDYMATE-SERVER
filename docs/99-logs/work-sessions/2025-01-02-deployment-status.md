# 배포 상태 및 OAuth 테스트 계획

**날짜**: 2025-01-02  
**상태**: 🔄 배포 진행 중  
**담당자**: minhan (인프라/DevOps)

## 🚀 현재 배포 상황

### 완료된 코드 수정사항:
- ✅ **OAuth 리다이렉트 URI**: 모든 설정 파일에 `/api/v1` 프리픽스 추가
- ✅ **SecurityConfig**: `/api/v1/health` 엔드포인트 허용 추가  
- ✅ **외부 OAuth 설정**: 네이버/구글 개발자 콘솔에서 Callback URL 업데이트 완료
- ✅ **Git 커밋**: 모든 변경사항 main 브랜치에 푸시 완료

### 배포 진행 상황:
- ✅ **배포 트리거**: GitHub Actions 자동 배포 시작됨 (커밋: a685ed5)
- 🔄 **배포 진행 중**: 현재 구버전이 여전히 서비스 중

```bash
# 현재 서버 응답 (구버전)
curl -I https://api.languagemate.kr/api/v1/login/naver
# Location: ...redirect_uri=https://api.languagemate.kr/login/oauth2/code/naver

# 배포 완료 후 예상 응답 (신버전)
# Location: ...redirect_uri=https://api.languagemate.kr/api/v1/login/oauth2/code/naver
```

## 📋 배포 완료 후 테스트 계획

### 1. Health 엔드포인트 테스트
```bash
# 이전: 401 Unauthorized
# 예상: 200 OK
curl https://api.languagemate.kr/api/v1/health
```

### 2. OAuth 로그인 플로우 테스트

#### 2.1 네이버 로그인
```bash
# 1) 로그인 시작 URL 확인
curl -I https://api.languagemate.kr/api/v1/login/naver

# 예상 결과: redirect_uri에 /api/v1 포함
# Location: https://nid.naver.com/oauth2.0/authorize?...&redirect_uri=https://api.languagemate.kr/api/v1/login/oauth2/code/naver
```

#### 2.2 구글 로그인  
```bash
# 1) 로그인 시작 URL 확인
curl -I https://api.languagemate.kr/api/v1/login/google

# 예상 결과: redirect_uri에 /api/v1 포함
# Location: https://accounts.google.com/oauth2/auth?...&redirect_uri=https://api.languagemate.kr/api/v1/login/oauth2/code/google
```

### 3. 실제 로그인 테스트 (수동)
1. **브라우저에서 테스트**:
   - https://api.languagemate.kr/api/v1/login/naver
   - https://api.languagemate.kr/api/v1/login/google

2. **예상 플로우**:
   - 로그인 → OAuth 인증 → 콜백 → 토큰 발급 → 프론트엔드 리다이렉트

3. **성공 기준**:
   - ✅ 404 에러 없음
   - ✅ 토큰 정상 발급
   - ✅ 프론트엔드로 정상 리다이렉트

## 🔧 배포 완료 확인 방법

### 방법 1: API 응답 확인
```bash
# 배포 완료 시 redirect_uri가 변경됨
curl -I https://api.languagemate.kr/api/v1/login/naver 2>/dev/null | grep location
```

### 방법 2: Health 체크 확인  
```bash
# 배포 완료 시 401 → 200으로 변경
curl -s -o /dev/null -w "%{http_code}" https://api.languagemate.kr/api/v1/health
```

## ⚠️ 문제 발생 시 대응 방안

### 1. 배포 실패 시:
- GitHub Actions 로그 확인
- 수동 배포 고려

### 2. OAuth 404 에러 지속 시:
- 서버 로그 확인  
- SecurityConfig 설정 재검토

### 3. Health 엔드포인트 401 지속 시:
- SecurityConfig 적용 여부 확인
- 서버 재시작 고려

## 📝 예상 해결 효과

### 해결될 문제들:
- ❌ `GET /api/v1/health 401 (Unauthorized)`
- ❌ `Error: No refresh token`
- ❌ `토큰 재발급 실패, 로그인 페이지로 이동`
- ❌ OAuth 리다이렉트 404 에러

### 정상화될 기능들:
- ✅ 자동 Health 체크
- ✅ 네이버 OAuth 로그인
- ✅ 구글 OAuth 로그인  
- ✅ 토큰 갱신 시스템

## 🔧 추가 수정사항 (SecurityConfig 경로 통합)

### 근본 원인 분석:
- **클라이언트**: `baseURL = (VITE_API_URL || "/api") + "/v1"`
- **프로덕션**: `VITE_API_URL = "https://api.languagemate.kr"`
- **결과**: 클라이언트가 `/v1/*` 경로로 요청, 서버는 `/api/v1/*`만 허용

### SecurityConfig 최종 수정:
```java
// 모든 주요 경로에 대해 /v1/* 과 /api/v1/* 모두 허용
.requestMatchers("/v1/login/**", "/api/v1/login/**", "/v1/auth/**", "/api/v1/auth/**").permitAll()
.requestMatchers("/health", "/v1/health", "/api/v1/health", "/actuator/health").permitAll()
.requestMatchers("/login/oauth2/code/**", "/v1/login/oauth2/code/**", "/api/v1/login/oauth2/code/**").permitAll()

// 온보딩 API들도 양쪽 경로 모두 허용
.requestMatchers("/v1/onboarding/interest/motivations", "/api/v1/onboarding/interest/motivations", ...)
```

## 🚀 최종 배포 상태

### 커밋 이력:
- ✅ `da4f411`: OAuth 리다이렉트 URI 수정
- ✅ `9b5a10f`: SecurityConfig /api/v1/health 허용  
- ✅ `a685ed5`: 배포 트리거 (빈 커밋)
- ✅ `b3a4488`: SecurityConfig /v1/* 경로 허용 추가

### 배포 대기 중:
- 🔄 **GitHub Actions 배포 진행 중**
- ⏳ **완료 시점**: 5-10분 예상

---

**현재 상태**: 최종 수정 완료, 배포 대기 중  
**다음 작업**: 배포 완료 후 전체 시스템 검증