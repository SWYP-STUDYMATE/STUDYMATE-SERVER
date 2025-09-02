# OAuth 리다이렉트 URI API 경로 통합 작업

**날짜**: 2025-01-02  
**상태**: ✅ 완료  
**담당자**: minhan (인프라/DevOps)

## 🔧 수정 완료 사항

### 1. application-prod.yml 설정 파일 수정
```yaml
# 수정 전
naver:
  redirect_uri: ${NAVER_REDIRECT_URI:https://api.languagemate.kr/login/oauth2/code/naver}
google:
  redirect_uri: ${GOOGLE_REDIRECT_URI:https://api.languagemate.kr/login/oauth2/code/google}

# 수정 후
naver:
  redirect_uri: ${NAVER_REDIRECT_URI:https://api.languagemate.kr/api/v1/login/oauth2/code/naver}
google:
  redirect_uri: ${GOOGLE_REDIRECT_URI:https://api.languagemate.kr/api/v1/login/oauth2/code/google}
```

### 2. GitHub Actions 배포 설정 수정
**파일**: `.github/workflows/deploy.yml`

```properties
# 수정 전
naver.redirect_uri=https://api.languagemate.kr/login/oauth2/code/naver
google.redirect_uri=https://api.languagemate.kr/login/oauth2/code/google

# 수정 후
naver.redirect_uri=https://api.languagemate.kr/api/v1/login/oauth2/code/naver
google.redirect_uri=https://api.languagemate.kr/api/v1/login/oauth2/code/google
```

## 📋 외부 OAuth 제공업체 설정 업데이트 필요

### 1. 네이버 개발자 센터 설정 변경
1. [네이버 개발자 센터](https://developers.naver.com/) 접속
2. 애플리케이션 관리 → STUDYMATE 애플리케이션 선택
3. **Callback URL** 수정:
   - **기존**: `https://api.languagemate.kr/login/oauth2/code/naver`
   - **신규**: `https://api.languagemate.kr/api/v1/login/oauth2/code/naver`

### 2. Google Cloud Console 설정 변경
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. APIs & Services → Credentials → OAuth 2.0 Client IDs 선택
3. **승인된 리디렉션 URI** 수정:
   - **기존**: `https://api.languagemate.kr/login/oauth2/code/google`
   - **신규**: `https://api.languagemate.kr/api/v1/login/oauth2/code/google`

## 🚀 배포 순서

### 1. 서버 설정 배포
```bash
# main 브랜치로 푸시하면 자동 배포됨
git push origin main
```

### 2. 외부 OAuth 제공업체 설정 업데이트
- ✅ **네이버**: Callback URL 업데이트 필요
- ✅ **구글**: Redirect URI 업데이트 필요

### 3. 배포 후 테스트
```bash
# 배포 완료 후 OAuth 로그인 테스트
curl -I https://api.languagemate.kr/api/v1/login/naver
curl -I https://api.languagemate.kr/api/v1/login/google
```

## ✅ 완료된 수정 사항

### 서버 측 모든 OAuth 경로 통합 완료:
- ✅ `LoginController.java` - `/api/v1` 프리픽스 적용
- ✅ `application-prod.yml` - 설정 파일 redirect_uri 업데이트
- ✅ `deploy.yml` - GitHub Actions 배포 설정 업데이트
- ✅ Git 커밋 완료

### API 경로 일치성:
- ✅ **로그인 시작**: `/api/v1/login/naver`, `/api/v1/login/google`
- ✅ **OAuth 콜백**: `/api/v1/login/oauth2/code/naver`, `/api/v1/login/oauth2/code/google`
- ✅ **토큰 갱신**: `/api/v1/auth/refresh`

## ⚠️ 주의사항

### 배포 순서 중요:
1. **서버 배포 먼저** (이미 완료)
2. **외부 OAuth 설정 변경 후** (사용자 액션 필요)
3. **테스트 진행**

### 임시 에러 발생 가능:
- 외부 OAuth 설정 변경 전까지는 404 에러 발생 가능
- 설정 변경 후 정상 작동 예상

---

**다음 단계**: 외부 OAuth 제공업체에서 Callback URL/Redirect URI 업데이트