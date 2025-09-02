# 토큰 갱신 무한 루프 문제 해결

**날짜**: 2025-01-02  
**상태**: ✅ 해결 완료  
**담당자**: minhan (DevOps)

## 🚨 문제 상황

```
POST https://api.languagemate.kr/v1/auth/refresh 401 (Unauthorized)
무한 리로딩 진행중입니다.
```

사용자가 로그인 후 브라우저 콘솔에서 `/auth/refresh` 엔드포인트의 401 에러가 무한 반복되는 현상 발견.

## 🔍 원인 분석

### 1. 서버 사이드 문제
**파일**: `/src/main/java/com/studymate/domain/user/service/TokenServiceImpl.java`

```java
// 문제 코드 (38번 줄)
return TokenResponse.of(newAccessToken, null, userId);
```

**문제점**: 
- refresh token 갱신 시 새로운 refresh token을 생성하지 않고 `null`을 반환
- 클라이언트가 새로운 refresh token을 받지 못해 다음 갱신 요청 시 실패

### 2. 클라이언트 사이드 문제
**파일**: `/STYDYMATE-CLIENT/src/api/index.js`

```javascript
// 문제 코드 (133-137번 줄)
const res = await api.post(
  "/auth/refresh",
  null,
  { headers: { Authorization: `Bearer ${refreshToken}` } }
);
```

**문제점**:
- refresh 요청을 메인 `api` 인스턴스로 보내면서 무한 루프 발생
- refresh 요청이 401을 받으면 같은 인터셉터를 다시 거치게 되어 재귀 호출

## 🔧 해결 방안

### 1. 서버 사이드 수정

**TokenServiceImpl.java 수정**:
```java
// 수정 전
String newAccessToken = jwtUtils.generateAccessToken(userId);
return TokenResponse.of(newAccessToken, null, userId);

// 수정 후
String newAccessToken = jwtUtils.generateAccessToken(userId);
String newRefreshToken = jwtUtils.generateRefreshToken(userId);

// 새 리프레시 토큰을 Redis에 저장 (기존 토큰 교체)
refreshTokenRepository.save(
    RefreshToken.builder()
        .userId(userId.toString())
        .token(newRefreshToken)
        .ttlSeconds(TimeUnit.DAYS.toSeconds(7))
        .build());

return TokenResponse.of(newAccessToken, newRefreshToken, userId);
```

**변경사항**:
- ✅ 새로운 refresh token 생성 및 Redis 저장
- ✅ TokenResponse에 새로운 refresh token 포함
- ✅ 토큰 로테이션 보안 적용 (7일 TTL)

### 2. 클라이언트 사이드 수정

**index.js 수정**:
```javascript
// 수정 전: 무한 루프 위험
const res = await api.post("/auth/refresh", ...);

// 수정 후: 별도 인스턴스로 무한 루프 방지
const refreshApi = axios.create({
  baseURL: (import.meta.env.VITE_API_URL || "/api") + "/v1",
});

const res = await refreshApi.post("/auth/refresh", ...);
```

**변경사항**:
- ✅ 401 에러 처리에서 별도 axios 인스턴스 사용
- ✅ 403 에러 처리에서도 동일하게 적용
- ✅ 무한 재귀 호출 방지

## 📊 수정 전후 비교

| 구분 | 수정 전 | 수정 후 |
|------|---------|---------|
| **서버** | refresh token null 반환 | 새 refresh token 생성/저장 |
| **클라이언트** | 메인 인터셉터로 refresh 요청 | 별도 인스턴스로 refresh 요청 |
| **결과** | 무한 루프 + 401 에러 | 정상적인 토큰 갱신 |

## ✅ 검증 결과

### 정상적인 토큰 갱신 플로우
1. **Access Token 만료** → 401 에러 발생
2. **Refresh 요청** → 별도 axios 인스턴스로 전송
3. **새 토큰 수신** → access token + refresh token 모두 갱신
4. **원본 요청 재시도** → 새 access token으로 성공

### 보안 향상
- **토큰 로테이션**: 매번 새로운 refresh token 발급
- **TTL 관리**: Redis에 7일 만료 시간 설정
- **무한 루프 방지**: 별도 axios 인스턴스 사용

## 🎯 예방 조치

### 1. 토큰 갱신 모니터링
- refresh 요청 성공률 모니터링
- 무한 루프 패턴 감지 로직

### 2. 테스트 케이스 추가
```javascript
// 토큰 갱신 단위 테스트
test('refresh token should generate new tokens', async () => {
  // TokenService.refreshToken() 테스트
});

// 무한 루프 방지 테스트  
test('should prevent infinite refresh loop', async () => {
  // axios 인터셉터 테스트
});
```

### 3. 로깅 강화
```java
// 서버 사이드 로깅
log.info("Token refresh successful for user: {}, new tokens generated", userId);

// 클라이언트 사이드 로깅
log.info("Token refresh completed, new tokens stored");
```

## 📝 관련 파일

### 수정된 파일
- ✅ `TokenServiceImpl.java` - refresh token 로직 수정
- ✅ `index.js` - 무한 루프 방지 로직 추가

### 확인된 파일 (수정 불필요)
- ✅ `TokenController.java` - 이미 `/api/v1/auth/refresh`로 올바르게 매핑됨
- ✅ `WebConfig.java` - CORS 설정 이미 적절함

## 🔚 결론

**주요 성과**:
- 토큰 갱신 무한 루프 문제 완전 해결 ✅
- 토큰 로테이션 보안 강화 ✅
- 클라이언트 안정성 개선 ✅

**영향**:
- 사용자 세션 안정성 향상
- 불필요한 API 호출 제거
- 서버 리소스 절약

---

**해결 완료**: 2025-01-02  
**다음 단계**: 추가 API 경로 표준화 완료