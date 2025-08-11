# GitHub Secrets 설정 가이드

GitHub Actions CI/CD 파이프라인 구동에 필요한 Secrets 목록입니다.
Settings > Secrets and variables > Actions에서 아래 값들을 설정해주세요.

## 🔐 필수 GitHub Secrets

### 1. NCP Container Registry 정보
| Secret Name | 설명 | 예시 값 |
|------------|------|---------|
| `NCP_ACCESS_KEY` | NCP 액세스 키 | `ABCD1234EFGH5678IJKL` |
| `NCP_SECRET_KEY` | NCP 시크릿 키 | `abcdefghijklmnopqrstuvwxyz1234567890ABCD` |
| `NCP_REGISTRY_URL` | Container Registry URL | `kr.ncr.ntruss.com` |
| `NCP_REGISTRY_NAMESPACE` | Registry 네임스페이스 | `studymate` 또는 사용자 계정명 |

### 2. NCP 서버 접속 정보
| Secret Name | 설명 | 예시 값 |
|------------|------|---------|
| `NCP_SERVER_HOST` | 배포 대상 서버 IP | `223.130.xxx.xxx` 또는 `api.languagemate.kr` |
| `NCP_SERVER_USER` | SSH 접속 사용자명 | `root` 또는 `ubuntu` |
| `NCP_SERVER_SSH_KEY` | SSH 개인키 (전체 내용) | `-----BEGIN RSA PRIVATE KEY-----`<br>`MIIEpAIBAAKCAQEA...`<br>`...전체 키 내용...`<br>`-----END RSA PRIVATE KEY-----` |

### 3. Application Configuration (가장 중요!)
| Secret Name | 설명 | 예시 값 |
|------------|------|---------|
| `APPLICATION_YML` | 전체 application.yml 파일 내용 | 아래 섹션 참고 |

**참고**: 데이터베이스, Redis, JWT, OAuth 등 모든 애플리케이션 설정은 APPLICATION_YML 파일 내에 포함됩니다.

## 📝 설정 방법

1. GitHub 리포지토리로 이동
2. Settings → Secrets and variables → Actions 클릭
3. "New repository secret" 버튼 클릭
4. Name과 Value 입력 후 "Add secret" 클릭
5. 위 표의 모든 항목 반복

## ⚠️ 주의사항

- **SSH 키**: 개인키 전체 내용을 복사할 때 첫 줄과 마지막 줄(`-----BEGIN/END-----`) 포함
- **비밀번호**: 특수문자가 포함된 경우 따옴표 처리 불필요 (GitHub가 자동 처리)
- **URL**: 프로토콜(`https://`) 포함 여부는 애플리케이션 설정에 따라 결정
- **보안**: 절대 실제 값을 코드나 문서에 커밋하지 마세요

## 🔍 값 확인 방법

### NCP Console에서 확인
1. **Container Registry**: Console → Container Registry → Registry 정보
2. **Server**: Console → Server → 서버 상세 정보
3. **Cloud DB**: Console → Cloud DB for MySQL/Redis → 접속 정보

### 로컬에서 확인
```bash
# SSH 키 확인
cat ~/.ssh/your-private-key

# 현재 사용 중인 환경 변수 (개발 환경)
cat .env.dev
```

## 📄 APPLICATION_YML Secret 설정 방법

1. `src/main/resources/application.yml.example` 파일을 복사
2. 실제 값으로 수정 (특히 민감한 정보들)
3. 전체 파일 내용을 복사
4. GitHub Secret에 `APPLICATION_YML` 이름으로 저장

**주의**: 여러 줄 텍스트이므로 GitHub UI에서 입력 시 전체 내용을 그대로 붙여넣기

예시 (실제 값으로 변경 필요):
```yaml
server:
  port: 8080

spring:
  application:
    name: studymate-server
  datasource:
    url: jdbc:mysql://db-xxxxx.vpc-cdb.ntruss.com:3306/studymate_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: studymate
    password: RealPassword123!
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false  # 프로덕션에서는 false 권장

naver:
  client_id: EzXpYAkIVhX1ViFQ5edo
  client_secret: Zml5tphcOJ
  authorization_grant_type: authorization_code
  redirect_uri: "https://api.languagemate.kr/login/oauth2/code/naver"

google:
  client_id: 538467097495-xxxxx.apps.googleusercontent.com
  client_secret: GOCSPX-xxxxxxxxxxxxx
  redirect_uri: "https://api.languagemate.kr/login/oauth2/code/google"

jwt:
  secret_key: your-very-long-secret-key-at-least-256-bits-for-production

redis:
  host: redis-xxxxx.vpc-cdb.ntruss.com
  port: 6379
  password: RedisPassword123

cloud:
  ncp:
    storage:
      region: kr-standard
      endpoint: https://kr.object.ncloudstorage.com
      access-key: ncp_iam_BPAMKR1FHfq8OtlptO2s
      secret-key: ncp_iam_BPKMKRIVHA3Sg1Rd0nLEMJLBS3mLodofDT
      bucket-name: languagemate-profile-img
```

## ✅ 체크리스트

설정 완료 후 확인:
- [ ] 모든 NCP 관련 키 설정
- [ ] 데이터베이스 접속 정보 설정
- [ ] JWT 시크릿 키 설정 (충분히 긴 랜덤 문자열)
- [ ] OAuth 클라이언트 정보 설정
- [ ] SSH 개인키 전체 내용 포함 확인