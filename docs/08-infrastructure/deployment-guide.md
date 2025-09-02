# 🚀 STUDYMATE-SERVER Docker Compose 배포 가이드

## 📅 문서 정보
- **최종 업데이트**: 2025-08-26
- **작성자**: minhan
- **목적**: Docker Compose 기반 프로덕션 배포 가이드

---

## 🎯 배포 아키텍처 개요

### 변경 사항
- **이전**: NCP Cloud DB for MySQL/Redis 분리 구성
- **현재**: Docker Compose 단일 서버 통합 구성

### 서비스 구성
```
┌─────────────────────────────────────┐
│     NCP Bastion Server (Ubuntu)     │
├─────────────────────────────────────┤
│  🐳 Docker Compose Services        │
│  ├── studymate-app (Spring Boot)   │
│  ├── studymate-mysql (MySQL 8.0)   │
│  ├── studymate-redis (Redis 7)     │
│  └── studymate-backup (Auto Backup)│
└─────────────────────────────────────┘
```

---

## 🔧 필수 GitHub Secrets 설정

### 기존 유지 Secrets
| Secret 명 | 설명 | 예시 |
|-----------|------|------|
| `APPLICATION_YML` | Spring Boot 전체 설정 파일 | (기존 유지) |
| `NCP_ACCESS_KEY` | NCP API Access Key | your-ncp-access-key |
| `NCP_SECRET_KEY` | NCP API Secret Key | your-ncp-secret-key |
| `NCP_REGISTRY_URL` | NCP Container Registry URL | `languagemate-server-cr.kr.ncr.ntruss.com` |
| `NCP_SERVER_HOST` | 배포 대상 서버 IP | `223.130.156.72` |
| `NCP_SERVER_USER` | SSH 사용자명 | `ubuntu` |
| `NCP_SERVER_PASSWORD` | SSH 패스워드 | (서버 패스워드) |

### 새로 추가할 Secrets
| Secret 명 | 설명 | 예시 |
|-----------|------|------|
| `DB_ROOT_PASSWORD` | MySQL Root 패스워드 | `your-strong-root-password` |
| `DB_USER` | MySQL 애플리케이션 사용자 | `studymate` |
| `DB_PASSWORD` | MySQL 애플리케이션 패스워드 | `your-strong-db-password` |
| `REDIS_PASSWORD` | Redis 패스워드 | `your-strong-redis-password` |

---

## 📋 서버 초기 설정

### 1. 서버 준비 작업
```bash
# 서버 접속
ssh ubuntu@223.130.156.72

# 패키지 업데이트
sudo apt update && sudo apt upgrade -y

# Docker & Docker Compose 설치 확인
docker --version
docker-compose --version

# 프로젝트 디렉토리 생성
mkdir -p /home/ubuntu/studymate-server
cd /home/ubuntu/studymate-server

# 데이터 볼륨 디렉토리 생성
sudo mkdir -p /home/ubuntu/studymate-data/{mysql,redis}
sudo chown -R ubuntu:ubuntu /home/ubuntu/studymate-data

# Git 클론 (최초 1회)
git clone https://github.com/SWYP-STUDYMATE/STUDYMATE-SERVER.git .
```

### 2. 권한 설정
```bash
# backup.sh 실행 권한 부여
chmod +x scripts/backup.sh

# 로그 디렉토리 생성
mkdir -p logs uploads backups
```

---

## 🚀 배포 프로세스

### GitHub Actions를 통한 자동 배포
1. `main` 브랜치에 push 또는 manual trigger
2. GitHub Actions가 자동으로:
   - 프로젝트 빌드
   - Docker 이미지 생성 및 Registry 푸시
   - 서버에 SSH 접속하여 Docker Compose로 배포

### 수동 배포 (필요시)
```bash
cd /home/ubuntu/studymate-server

# 최신 코드 업데이트
git pull origin main

# 환경 변수 파일 생성 (.env)
cat > .env << EOF
REGISTRY_URL=languagemate-server-cr.kr.ncr.ntruss.com
IMAGE_NAME=studymate-server
DB_ROOT_PASSWORD=your-strong-root-password
DB_USER=studymate
DB_PASSWORD=your-strong-db-password
REDIS_PASSWORD=your-strong-redis-password
BACKUP_RETENTION_DAYS=7
EOF

# application.yml 파일 배치 (GitHub Actions에서 자동 생성)
# echo "..." > application.yml

# Docker Registry 로그인
echo "YOUR_NCP_SECRET_KEY" | docker login languagemate-server-cr.kr.ncr.ntruss.com -u YOUR_NCP_ACCESS_KEY --password-stdin

# 서비스 배포
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# 상태 확인
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f app
```

---

## 🔍 운영 및 모니터링

### 서비스 상태 확인
```bash
# 모든 컨테이너 상태
docker-compose -f docker-compose.prod.yml ps

# 개별 서비스 로그 확인
docker-compose -f docker-compose.prod.yml logs app
docker-compose -f docker-compose.prod.yml logs db
docker-compose -f docker-compose.prod.yml logs redis

# 실시간 로그 모니터링
docker-compose -f docker-compose.prod.yml logs -f --tail 50 app
```

### Health Check
```bash
# 애플리케이션 헬스 체크
curl -f http://localhost:8080/actuator/health

# 외부 접속 테스트 (Cloudflare Tunnel 통해)
curl -f https://api.languagemate.kr/actuator/health
```

### 데이터베이스 접근
```bash
# MySQL 접속
docker-compose -f docker-compose.prod.yml exec db mysql -u studymate -p studymate

# Redis 접속
docker-compose -f docker-compose.prod.yml exec redis redis-cli -a your-redis-password
```

---

## 💾 백업 관리

### 자동 백업
- **주기**: 매일 새벽 2시
- **보관 기간**: 7일
- **위치**: `/home/ubuntu/studymate-server/backups/`

### 수동 백업
```bash
# 즉시 백업 실행
docker-compose -f docker-compose.prod.yml exec db-backup /backup.sh

# 백업 파일 확인
ls -la backups/studymate_backup_*.sql.gz
```

### 복원 방법
```bash
# 백업에서 복원
gunzip < backups/studymate_backup_YYYYMMDD_HHMMSS.sql.gz | \
docker-compose -f docker-compose.prod.yml exec -T db mysql -u root -p
```

---

## 🔧 트러블슈팅

### 일반적인 문제 해결

#### 1. HikariCP "Sealed Pool" 오류 (✅ 해결됨)
**증상**: Spring Boot 애플리케이션이 시작되지 않고 HikariCP 관련 오류 발생
```
java.lang.IllegalStateException: The configuration of the pool is sealed once started
Property: spring.datasource.hikari.initialization-fail-timeout
```

**원인**: `initialization-fail-timeout` 속성이 HikariCP 풀 시작 후에 설정되려고 시도

**해결방법**: 
- GitHub Actions 워크플로우에서 해당 속성 제거
- `.github/workflows/deploy.yml` 파일의 `spring.datasource.hikari.initialization-fail-timeout=60000` 라인 삭제

**권장 HikariCP 설정**:
```properties
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
# initialization-fail-timeout 사용 금지
```

#### 2. 컨테이너가 시작되지 않는 경우
```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs

# 특정 서비스 재시작
docker-compose -f docker-compose.prod.yml restart app
```

#### 2. 데이터베이스 연결 실패
```bash
# MySQL 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml exec db mysqladmin -u studymate -p ping

# 네트워크 확인
docker network ls
docker network inspect studymate-network
```

#### 3. 디스크 공간 부족
```bash
# Docker 이미지/컨테이너 정리
docker system prune -af

# 로그 파일 정리
find logs/ -name "*.log" -mtime +30 -delete
```

### 롤백 절차
```bash
# 이전 이미지로 롤백
docker-compose -f docker-compose.prod.yml down
docker tag languagemate-server-cr.kr.ncr.ntruss.com/studymate-server:previous-version \
            languagemate-server-cr.kr.ncr.ntruss.com/studymate-server:latest
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📝 체크리스트

### 배포 전 확인사항
- [ ] GitHub Secrets 모든 값 설정 완료
- [ ] 서버 디스크 공간 충분 (최소 10GB 여유)
- [ ] 기존 데이터 백업 완료 (마이그레이션 시)
- [ ] 네트워크 연결 상태 정상

### 배포 후 확인사항
- [ ] 모든 컨테이너 정상 실행 (`docker-compose ps`)
- [ ] 애플리케이션 헬스 체크 통과
- [ ] 데이터베이스 연결 정상
- [ ] Redis 연결 정상
- [ ] 자동 백업 스케줄 동작
- [ ] 외부 API 접근 테스트 (https://api.languagemate.kr)

---

## 💡 주의사항

1. **시크릿 관리**: `.env` 파일과 `application.yml`는 절대 Git에 커밋하지 않음
2. **백업**: 중요한 변경 전 반드시 데이터베이스 백업 수행
3. **모니터링**: 배포 후 최소 1시간 동안 로그 모니터링 필요
4. **보안**: 강력한 패스워드 사용 및 정기적 변경
5. **용량**: 정기적인 로그 파일 및 백업 파일 정리 필요