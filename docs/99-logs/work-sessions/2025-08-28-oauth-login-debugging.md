# OAuth 로그인 디버깅 결과

## 작업 일시
- **날짜**: 2025-08-28
- **담당**: minhan (인프라/DevOps)

## 문제 상황
사용자가 프로덕션 환경에서 OAuth 로그인이 작동하지 않는다고 보고

## 발견된 문제들

### 1. LoginController OAuth 엔드포인트 구현 문제 ✅ 해결완료
**문제**: OAuth 로그인 엔드포인트가 HTTP 리다이렉트 대신 OAuth URL 문자열만 반환
```java
// 기존 (문제)
@GetMapping("api/v1/login/naver")
public String naverLoginPage() {
    return loginService.getLoginUrl("naver",state, naverClientId, naverRedirectUri);
}

// 수정 후 (해결)  
@GetMapping("api/v1/login/naver")
public void naverLoginPage(HttpServletResponse response) throws IOException {
    String loginUrl = loginService.getLoginUrl("naver",state, naverClientId, naverRedirectUri);
    response.sendRedirect(loginUrl);
}
```

### 2. OAuth 콜백 URL 하드코딩 문제 ✅ 해결완료
**문제**: 프로덕션에서도 localhost:3000으로 리다이렉트하도록 하드코딩됨
```java
// 기존 (문제)
String redirectUrl = UriComponentsBuilder
    .fromUriString("http://localhost:3000/main")
    .queryParam("accessToken", tokens.accessToken())
    .build().toUriString();

// 수정 후 (해결)
String redirectUrl = UriComponentsBuilder  
    .fromUriString("https://languagemate.kr/main")
    .queryParam("accessToken", tokens.accessToken())
    .build().toUriString();
```

### 3. API 라우팅 문제 ⚠️ 지속중 - 인프라 수정 필요
**문제**: OAuth API 엔드포인트가 백엔드로 라우팅되지 않음

#### 현재 상황:
- `https://api.languagemate.kr/api/v1/login/naver` → 502 Bad Gateway 
- `https://languagemate.kr/api/v1/login/naver` → HTML 반환 (프론트엔드로 라우팅)

#### 원인:
로드밸런서 또는 프록시 설정에서 `/api/v1/login/*` 경로가 백엔드 서버로 라우팅되지 않고 있음

#### 해결 방법:
1. **NCP Load Balancer** 설정에서 API 경로 라우팅 규칙 추가
2. **CloudFlare** 또는 기타 CDN/프록시에서 API 경로 우선순위 조정
3. **Docker 네트워크** 설정에서 API 서버 연결 상태 확인

## 테스트 결과

### OAuth 플로우 테스트
```bash
# 1. 직접 API 도메인 접근
curl -I https://api.languagemate.kr/api/v1/login/naver
# 결과: HTTP/2 502 (Bad Gateway)

# 2. 프론트엔드 도메인을 통한 접근  
curl -I https://languagemate.kr/api/v1/login/naver  
# 결과: HTTP/2 200, content-type: text/html (잘못된 라우팅)
```

## 권장 조치사항

### 즉시 조치 (인프라팀 - minhan)
1. **NCP Load Balancer 설정 점검**
   - Target Group에서 백엔드 서버 헬스체크 상태 확인
   - API 경로(`/api/*`)에 대한 라우팅 규칙 추가
   
2. **Docker 서비스 상태 확인**
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   docker-compose -f docker-compose.prod.yml logs app
   ```

3. **네트워크 연결 테스트**
   - 백엔드 서버가 실제로 실행 중인지 확인
   - 내부 네트워크에서 API 서버 접근 가능한지 테스트

### 장기 개선사항
1. **Health Check 엔드포인트 활용**
   - `/actuator/health`를 활용한 로드밸런서 헬스체크 
   
2. **API Gateway 패턴 도입**
   - 모든 API 요청을 단일 진입점으로 통합
   - 라우팅 규칙 명확화

## 커밋 정보
- **커밋 해시**: 4688021
- **메시지**: "fix: resolve OAuth login issues in production environment"
- **변경 파일**: `src/main/java/com/studymate/domain/user/controller/LoginController.java`

## 다음 단계
1. 인프라 설정 수정 후 OAuth 로그인 재테스트
2. 실제 브라우저에서 전체 OAuth 플로우 검증
3. 사용자에게 로그인 재테스트 요청

## 학습 사항
- 프로덕션 환경에서는 코드 수정만으로는 불충분
- 인프라 계층의 라우팅 설정이 애플리케이션 기능에 직접적 영향
- OAuth 같은 리다이렉트 기반 인증은 전체 요청 흐름 검증 필요