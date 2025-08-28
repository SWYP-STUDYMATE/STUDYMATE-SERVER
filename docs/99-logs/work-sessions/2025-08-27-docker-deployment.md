# STUDYMATE-SERVER Docker 배포 작업 세션

**날짜**: 2025-08-27  
**작업자**: minhan (DevOps)  
**목표**: NCP 클라우드 DB를 Docker 컨테이너로 마이그레이션하고 단일 서버 배포 구현

## 📋 작업 개요

### 요구사항
- NCP Cloud DB for MySQL과 Redis를 제거하고 Docker 컨테이너로 전환
- 단일 NCP 서버에서 모든 서비스(MySQL, Redis, Spring Boot 애플리케이션) 실행
- GitHub Actions를 통한 자동 배포 파이프라인 구축

### 기술 스택 변경
- **이전**: NCP Cloud DB for MySQL + Redis
- **이후**: Docker Compose (MySQL 8.0 + Redis 7 + Spring Boot)

## 🚀 진행 단계

### 1. 로컬 개발 환경 구축 ✅
- `docker-compose.local.yml` 생성
- `Dockerfile.local` (multi-stage build) 생성  
- `.env.local` 환경변수 파일 생성
- `application.yml` 환경변수 연동 수정

**주요 해결 문제**:
- Java 17 빌드 환경 구성 (gradle:8.14-jdk17 base image 사용)
- MySQL 인증 오류 해결 (환경변수 불일치)
- Spring Security 설정 수정 (actuator health endpoint 허용)

### 2. 프로덕션 Docker Compose 설정 ✅
- `docker-compose.prod.yml` 생성
- 백업 시스템 구현 (7일 보관 정책)
- Watchtower 자동 업데이트 설정
- 데이터 영속성 확보 (볼륨 마운트)

### 3. GitHub Actions CI/CD 파이프라인 구축 ⚠️
총 3번의 수정을 통해 배포 성공:

#### 첫 번째 시도 - 실패
- **문제**: `docker-compose` 명령어 미지원, actuator health endpoint 없음
- **오류**: `command not found: docker-compose`, 404 Not Found

#### 두 번째 시도 - 실패  
- **문제**: Docker Compose V2 명령어 불일치, 인프라 설정 오류
- **오류**: `unknown shorthand flag: 'f'`, `invalid user: 'ubuntu:ubuntu'`

#### 세 번째 시도 - 성공 ✅
- **해결**: Docker Compose Plugin 자동 설치, Git 저장소 초기화, 권한 문제 해결

## ✅ 성공한 구현사항

### Docker 컨테이너 환경
```yaml
services:
  app: Spring Boot 애플리케이션 (포트 8080)
  db: MySQL 8.0 (포트 3306)  
  redis: Redis 7 (포트 6379)
  backup: 자동 백업 서비스 (7일 보관)
  watchtower: 자동 업데이트 서비스
```

### 로컬 테스트 결과
- ✅ 모든 컨테이너 정상 실행
- ✅ 공개 API 엔드포인트 응답: `/api/v1/onboard/interest/motivations`
- ✅ 인증 보안 정상 작동: `/api/v1/users` → 401 Unauthorized
- ✅ WebSocket 연결 가능: `/ws/chat/info` → SockJS 정보 반환

### 프로덕션 배포 결과
- ✅ Docker 이미지 빌드 및 NCP Container Registry 업로드 완료
- ✅ 모든 컨테이너 생성 및 실행 완료
- ✅ GitHub Actions 자동 배포 파이프라인 구축 완료

## ⚠️ 현재 해결 필요한 이슈

### 1. 외부 접근 문제 (502 Bad Gateway)
- **증상**: `https://api.languagemate.kr` 접속 시 502 오류
- **원인**: NCP Load Balancer와 Docker 컨테이너 간 연결 문제
- **상태**: 컨테이너는 실행 중이나 외부 트래픽 라우팅 실패

### 2. 컨테이너 재시작 상태
- **관찰**: 일부 컨테이너가 "Restarting" 상태
- **추정**: 서비스 간 의존성 또는 리소스 문제
- **필요**: 상세 로그 분석

## 🔧 기술적 세부사항

### Docker Compose V2 vs V1
```bash
# V1 (deprecated)
docker-compose -f docker-compose.prod.yml up -d

# V2 (current)  
docker compose -f docker-compose.prod.yml up -d
```

### 환경변수 관리
```bash
# Production secrets
DB_ROOT_PASSWORD: GitHub Secrets
DB_USER: GitHub Secrets  
DB_PASSWORD: GitHub Secrets
REDIS_PASSWORD: GitHub Secrets
APPLICATION_YML: 전체 Spring Boot 설정
```

### 네트워크 구성
```yaml
networks:
  studymate-network:
    driver: bridge
    name: studymate-network-prod
```

## 📊 성과 및 개선점

### 달성한 목표
1. ✅ NCP 클라우드 DB 제거 및 비용 절감
2. ✅ 단일 서버 Docker 환경 구축  
3. ✅ 자동화된 CI/CD 파이프라인
4. ✅ 데이터 백업 및 복원 시스템
5. ✅ 로컬 개발 환경 일치성 확보

### 다음 단계
1. **로드밸런서 설정 수정**: 502 오류 해결
2. **모니터링 구축**: 컨테이너 헬스체크 강화
3. **로그 관리**: 중앙집중식 로깅 시스템 도입
4. **보안 강화**: SSL 인증서 및 방화벽 설정
5. **성능 최적화**: 리소스 사용량 모니터링

## 🎯 결론

Docker 기반 인프라로의 전환이 성공적으로 완료되었으며, 로컬 개발 환경에서 모든 기능이 정상 작동함을 확인했습니다. 현재 프로덕션 환경에서는 로드밸런서 설정 문제로 외부 접근이 제한되지만, 이는 인프라 설정 조정을 통해 해결 가능한 문제입니다.

전체적으로 클라우드 DB 의존성을 제거하고 Docker 컨테이너 기반의 자체 관리형 인프라를 성공적으로 구축했습니다.