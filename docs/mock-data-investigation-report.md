# Mock 데이터 조사 보고서

## 조사 일시
2025년 9월 16일

## 조사 범위
- **백엔드 (Spring Boot)**: `/src/main/java` 디렉토리
- **프론트엔드 (React)**: `/src` 디렉토리
- **Workers**: `/workers/src` 디렉토리

## 조사 결과

### 1. 백엔드 (Spring Boot)

#### application.yml 설정
```yaml
features:
  enable-mock-mode: ${ENABLE_MOCK_MODE:false}
```
- **위치**: `/src/main/resources/application.yml:101`
- **상태**: 기본값 `false`로 설정됨
- **사용처**: 코드에서 실제로 사용되는 곳 없음 (검색 결과 없음)

#### Mock 데이터 존재 여부
- **서비스 레이어**: Mock 데이터 반환 코드 **없음**
- **컨트롤러**: 하드코딩된 응답 **없음**
- **Repository**: 더미 데이터 **없음**

#### 테스트 코드
- **위치**: `/src/test/java`
- **상태**: 테스트용 Mock 데이터는 존재하나, 프로덕션 코드에는 영향 없음

### 2. 프론트엔드 (React)

#### Mock 데이터 검색 결과
- **컴포넌트**: Mock 데이터 사용 **없음**
- **API 호출**: 실제 백엔드 API 호출 중
- **상태 관리**: Zustand store에 하드코딩된 데이터 **없음**

#### E2E 테스트
- **위치**: `/e2e` 디렉토리
- **상태**: 테스트용 데이터는 존재하나, 프로덕션 코드와 분리됨

### 3. Workers (Cloudflare)

#### Mock 데이터 검색 결과
- **라우트**: 모든 API가 실제 로직 구현됨
- **서비스**: AI 모델 또는 실제 데이터 처리 중
- **응답**: 하드코딩된 응답 **없음**

## 로그인 문제와의 연관성

### 401 Unauthorized 에러 분석
```
POST https://api.languagemate.kr/api/v1/onboarding/steps/1/save 401 (Unauthorized)
```

#### 가능한 원인 (Mock 데이터와 무관)
1. **JWT 토큰 만료**: 세션 타임아웃
2. **Redis 세션 문제**: 세션 스토어 연결 이슈
3. **인증 필터 문제**: Spring Security 설정
4. **CORS 설정**: 인증 헤더 누락

#### Mock 데이터와의 연관성
- **결론**: **연관성 없음**
- 프로덕션 코드에 Mock 데이터가 활성화되어 있지 않음
- `enable-mock-mode`는 false로 설정되어 있고, 실제 사용되지 않음

## 권장 조치

### 로그인 문제 해결
1. 브라우저 개발자 도구에서 JWT 토큰 확인
2. Redis 서버 상태 확인
3. Spring Boot 로그에서 인증 관련 에러 확인

### Mock 모드 정리
1. `enable-mock-mode` 설정 제거 권장 (사용되지 않음)
2. 테스트 코드와 프로덕션 코드 명확히 분리 유지

## 결론

**프로젝트 전체에서 프로덕션용 Mock 데이터는 발견되지 않았습니다.**

- 백엔드: Mock 모드 설정은 있으나 실제 구현 없음
- 프론트엔드: 실제 API 호출 중
- Workers: 모든 API 정상 구현됨

로그인 401 에러는 Mock 데이터와 관련이 없으며, 인증/세션 관련 이슈로 판단됩니다.