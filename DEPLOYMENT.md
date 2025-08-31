# STUDYMATE 프로덕션 배포 가이드

## 🚨 502 Bad Gateway 해결 방안

현재 502 에러는 애플리케이션이 H2 인메모리 데이터베이스로 설정되어 있어 MySQL/Redis를 사용하지 않기 때문입니다.

### 즉시 해결 단계

#### 1. 프로덕션 환경 설정 파일 생성

```bash
# 서버에서 실행
cd /path/to/studymate-server
cp .env.prod.example .env.prod
```

#### 2. `.env.prod` 파일 편집

```bash
# 실제 값으로 수정
nano .env.prod
```

필수 설정값:
- `DB_USER`, `DB_PASSWORD`, `DB_ROOT_PASSWORD` - MySQL 접속 정보
- `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET` - Naver OAuth
- `JWT_SECRET` - 256비트 이상의 안전한 키
- `NCP_ACCESS_KEY`, `NCP_SECRET_KEY` - NCP Object Storage

#### 3. 컨테이너 재시작

```bash
# 전체 재시작 (권장)
./scripts/deploy.sh restart

# 또는 수동 재시작
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

#### 4. 상태 확인

```bash
# 종합 진단
./scripts/deploy.sh diagnose

# 개별 확인
./scripts/deploy.sh status   # 컨테이너 상태
./scripts/deploy.sh logs     # 로그 확인
./scripts/deploy.sh health   # 헬스 체크
```

## 📋 배포 스크립트 사용법

### 기본 명령어

```bash
./scripts/deploy.sh {command}
```

### 사용 가능한 명령어

| 명령어 | 설명 | 사용 예시 |
|--------|------|-----------|
| `start` | 모든 컨테이너 시작 | `./scripts/deploy.sh start` |
| `stop` | 모든 컨테이너 중지 | `./scripts/deploy.sh stop` |
| `restart` | 전체 재시작 | `./scripts/deploy.sh restart` |
| `status` | 컨테이너 상태 확인 | `./scripts/deploy.sh status` |
| `logs` | 컨테이너 로그 확인 | `./scripts/deploy.sh logs` |
| `diagnose` | 종합 진단 실행 | `./scripts/deploy.sh diagnose` |
| `health` | 애플리케이션 헬스 체크 | `./scripts/deploy.sh health` |

## 🔍 문제 진단 체크리스트

### 1. 환경 변수 확인
```bash
# .env.prod 파일 존재 확인
ls -la .env.prod

# 환경 변수 로딩 확인
docker-compose -f docker-compose.prod.yml config
```

### 2. 컨테이너 상태 확인
```bash
# 컨테이너 실행 상태
docker-compose -f docker-compose.prod.yml ps

# 헬스 체크 상태
docker-compose -f docker-compose.prod.yml ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"
```

### 3. 로그 분석
```bash
# 애플리케이션 로그
docker-compose -f docker-compose.prod.yml logs -f app

# 데이터베이스 연결 로그 확인
docker-compose -f docker-compose.prod.yml logs db | grep -i "ready for connections"

# Redis 로그
docker-compose -f docker-compose.prod.yml logs redis
```

### 4. 네트워크 연결 테스트
```bash
# 데이터베이스 연결 테스트
docker-compose -f docker-compose.prod.yml exec db mysql -u studymate -p -e "SELECT 1;"

# Redis 연결 테스트
docker-compose -f docker-compose.prod.yml exec redis redis-cli ping

# 애플리케이션 헬스 체크
curl http://localhost:8080/actuator/health
```

## 🚀 정상 배포 프로세스

### 1. 사전 준비
```bash
# 프로젝트 디렉토리로 이동
cd /path/to/studymate-server

# 환경 변수 파일 설정
cp .env.prod.example .env.prod
# .env.prod 파일을 실제 값으로 수정

# 필요 디렉토리 생성
mkdir -p logs uploads backups
mkdir -p /home/ubuntu/studymate-data/mysql
mkdir -p /home/ubuntu/studymate-data/redis
```

### 2. 배포 실행
```bash
# 이미지 빌드 및 배포
./scripts/deploy.sh restart

# 배포 확인
./scripts/deploy.sh diagnose
```

### 3. 배포 후 검증
```bash
# API 엔드포인트 테스트
curl https://api.languagemate.kr/actuator/health

# Swagger UI 접속 확인
curl https://api.languagemate.kr/swagger-ui/index.html

# 로그 모니터링
./scripts/deploy.sh logs
```

## 📊 모니터링 및 유지보수

### 로그 모니터링
```bash
# 실시간 로그 모니터링
docker-compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그
docker-compose -f docker-compose.prod.yml logs -f app
```

### 백업 관리
```bash
# 수동 백업 실행
docker-compose -f docker-compose.prod.yml exec db-backup /backup.sh

# 백업 파일 확인
ls -la backups/
```

### 성능 모니터링
```bash
# 컨테이너 리소스 사용량
docker stats

# 디스크 사용량 확인
df -h
du -sh /home/ubuntu/studymate-data/
```

## ⚠️ 주요 주의사항

1. **환경 변수 보안**: `.env.prod` 파일에는 민감한 정보가 포함되므로 권한을 600으로 설정
   ```bash
   chmod 600 .env.prod
   ```

2. **데이터 백업**: 중요한 데이터베이스 변경 전 반드시 백업 실행

3. **로그 관리**: 로그 파일이 디스크를 가득 채우지 않도록 정기적으로 확인

4. **보안 업데이트**: 컨테이너 이미지 정기 업데이트

## 🆘 긴급 복구 절차

### 서비스 완전 중단 시
```bash
# 모든 컨테이너 중지
docker-compose -f docker-compose.prod.yml down

# 볼륨 및 네트워크 정리 (주의: 데이터 손실 가능)
docker-compose -f docker-compose.prod.yml down -v

# 완전 재시작
./scripts/deploy.sh restart
```

### 데이터베이스 복구
```bash
# 최신 백업에서 복구
docker-compose -f docker-compose.prod.yml exec db mysql -u root -p studymate < backups/latest_backup.sql
```

## 📞 지원 및 문의

문제가 지속될 경우:
1. 로그 파일 수집: `./scripts/deploy.sh diagnose > diagnosis.log`
2. 시스템 상태 확인: `docker system df`, `free -h`, `df -h`
3. 개발팀에 진단 로그와 함께 문의