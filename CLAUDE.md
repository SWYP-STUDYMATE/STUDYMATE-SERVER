# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🎯 프로젝트 개요

**STUDYMATE-SERVER**는 언어 교환 학습 플랫폼의 백엔드 API 서버입니다. 사용자 매칭, 실시간 채팅, 온보딩, 학습 관리 기능을 제공합니다.

### 담당 개발자
- **백엔드 개발자 A**: Java/Spring Boot 개발 담당
- **minhan (나)**: 인프라 및 DevOps 담당

### 관련 프로젝트
- **STUDYMATE-CLIENT**: React 기반 웹 클라이언트 (Cloudflare Pages)

## 📝 필수 상호 참조 규칙

**백엔드 개발 시 반드시 확인해야 할 클라이언트 관련 사항:**
- **TypeScript 인터페이스**: `../STYDYMATE-CLIENT/src/types/` DTO 응답과 일치 확인
- **컴포넌트 요구사항**: `../STYDYMATE-CLIENT/docs/06-frontend/components/` API 응답 형식 확인  
- **에러 처리**: `../STYDYMATE-CLIENT/src/utils/errorHandling.ts` 에러 코드 동기화
- **WebSocket 이벤트**: 클라이언트 이벤트 핸들러와 서버 이벤트 일치
- **상태 관리**: `../STYDYMATE-CLIENT/src/stores/` Zustand store와 서버 상태 동기화

**문서 업데이트 필수:**
- Controller 변경 → `docs/04-api/api-reference.md` 업데이트
- DTO 변경 → 클라이언트 TypeScript 인터페이스 확인 요청
- Entity 변경 → `docs/05-database/database-schema.md` 업데이트  
- 에러 코드 추가 → `docs/07-backend/error-handling.md` 업데이트

## 📦 기술 스택

### Core Technologies
- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: MySQL 8.0 (NCP Cloud DB for MySQL)
- **Cache**: Redis 7 (NCP Cloud DB for Redis)
- **Storage**: NCP Object Storage (AWS S3 호환)
- **Authentication**: JWT + Spring Security
- **External API**: Naver OAuth (OpenFeign)
- **WebSocket**: STOMP를 통한 실시간 채팅
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker + Docker Compose

## 🚀 개발 명령어

### 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행 (로컬)
./gradlew bootRun

# Docker 컨테이너 실행 (개발환경)
docker-compose -f docker-compose.dev.yml up -d

# Docker 컨테이너 중지
docker-compose -f docker-compose.dev.yml down
```

### 개발 환경 설정
- Java 17 필수
- 로컬 개발시 MySQL 8.0 및 Redis 7이 Docker Compose로 실행
- 애플리케이션은 8080 포트에서 실행
- MySQL은 3306, Redis는 6379 포트 사용

## 📁 프로젝트 구조

### 패키지 구조
```
com.studymate/
├── config/           # 설정 클래스들 (Security, Redis, WebSocket, Swagger)
├── common/          # 공통 컴포넌트 (DTO, Entity, Exception)
├── auth/           # 인증 관련 (JWT, OAuth)
├── domain/         # 비즈니스 도메인
│   ├── user/       # 사용자 관리 (Naver OAuth, JWT)
│   ├── chat/       # 실시간 채팅 (WebSocket)
│   ├── onboarding/ # 온보딩 설문조사
│   └── matching/   # 사용자 매칭 로직
└── exception/      # 전역 예외 처리
```

### 주요 도메인 구조

#### User 도메인
- **Naver OAuth 인증**: `NaverLoginController`, `NaverApi` (OpenFeign)
- **JWT 토큰 관리**: `TokenService`, `JwtUtils`, `JwtAuthenticationFilter`
- **사용자 프로필**: 위치, 영어명, 자기소개, 프로필 이미지

#### Chat 도메인
- **WebSocket 실시간 채팅**: `WebSocketConfig`, `ChatController`
- **채팅방 관리**: `ChatRoomController`, `ChatRoomService`
- **메시지 저장**: `ChatMessage`, `ChatRoom` 엔티티

#### Onboarding 도메인
- **언어 설정**: 학습 언어, 수준, 모국어
- **학습 스타일**: 동기, 학습 스타일, 소통 방법
- **파트너 선호도**: 성별, 성격 유형
- **스케줄 관리**: 요일별 학습 시간 설정

### 보안 설정
- **JWT 기반 Stateless 인증**
- **CSRF 비활성화** (API 서버)
- **공개 경로**: `/login/**`, `/auth/**`
- **모든 다른 엔드포인트는 인증 필요**

### 데이터베이스 설계
- **BaseTimeEntity**: 모든 엔티티의 생성/수정 시간 자동 관리
- **복합키 엔티티**: 온보딩 관련 매핑 테이블들 (`OnboardLangLevelId`, `OnboardMotivationId` 등)
- **MySQL**: 메인 데이터 저장소
- **Redis**: 세션 및 캐시 저장소

## 🌐 인프라 아키텍처 (NCP)

### 네트워크 구성
- **VPC**: studymate-vpc (10.0.0.0/16)
- **Public Subnet**: Load Balancer (10.0.1.0/24)
- **Private Subnet**: Application Server (10.0.2.0/24)
- **도메인**: api.languagemate.kr
- **SSL/TLS**: Load Balancer에서 SSL Offloading

### NCP 서비스 구성
```yaml
Production Environment:
  - Server: 2 vCPU, 4GB RAM (Auto-scaling enabled)
  - Load Balancer: Network Load Balancer
  - Database: Cloud DB for MySQL (Standard)
  - Cache: Cloud DB for Redis (Standard)
  - Storage: Object Storage (S3 호환)
  - CDN: Global CDN
```

### 환경 변수 관리
```bash
# Production (NCP)
SPRING_PROFILES_ACTIVE=prod
DB_HOST={NCP_MYSQL_ENDPOINT}
DB_PORT=3306
DB_NAME=studymate
REDIS_HOST={NCP_REDIS_ENDPOINT}
REDIS_PORT=6379
NCP_ACCESS_KEY={ACCESS_KEY}
NCP_SECRET_KEY={SECRET_KEY}
NCP_OBJECT_STORAGE_ENDPOINT=https://kr.object.ncloudstorage.com
NCP_BUCKET_NAME=studymate-storage

# OAuth
NAVER_CLIENT_ID={NAVER_CLIENT_ID}
NAVER_CLIENT_SECRET={NAVER_CLIENT_SECRET}
```

## 📚 API 문서화

### Swagger/OpenAPI
- **Local**: `http://localhost:8080/swagger-ui/index.html`
- **Production**: `https://api.languagemate.kr/swagger-ui/index.html`
- **OpenAPI 3**: SpringDoc으로 자동 생성

### API 엔드포인트 규칙
```
GET    /api/v1/users          # 리스트 조회
GET    /api/v1/users/{id}     # 단일 조회
POST   /api/v1/users          # 생성
PUT    /api/v1/users/{id}     # 전체 수정
PATCH  /api/v1/users/{id}     # 부분 수정
DELETE /api/v1/users/{id}     # 삭제
```

## ⚠️ 개발 시 주의사항

### 코드 규칙
- **Controller**: REST API 엔드포인트 정의
- **Service**: 비즈니스 로직 구현
- **Repository**: 데이터 접근 계층
- **DTO**: 데이터 전송 객체 (Request/Response 분리)
- **Entity**: JPA 엔티티 (BaseTimeEntity 상속)

### 보안 고려사항
- JWT 토큰은 Authorization 헤더로 전달 (`Bearer {token}`)
- 민감한 정보는 절대 로그에 남기지 않음
- 환경 변수는 `.env` 파일로 관리 (절대 커밋하지 않음)
- CORS 설정은 프론트엔드 도메인만 허용

### WebSocket 통신
- 연결 엔드포인트: `/ws`
- STOMP 프로토콜 사용
- 메시지 브로커: `/topic`, `/queue`
- 앱 목적지 접두사: `/app`

### 응답 형식
```java
// 성공 응답
{
  "success": true,
  "data": { ... },
  "message": "Success"
}

// 에러 응답
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message"
  }
}
```

## 🔄 Git 워크플로우

### 브랜치 전략
- `main`: 프로덕션 배포
- `develop`: 개발 통합
- `feature/{task-name}`: 기능 개발
- `hotfix/{issue-name}`: 긴급 수정

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
chore: 빌드 업무 수정
```

## 👥 협업 가이드

### 개발자별 책임 영역

#### 백엔드 개발자 A
- Spring Boot 애플리케이션 개발
- 비즈니스 로직 구현
- API 설계 및 구현
- 데이터베이스 스키마 설계

#### minhan (인프라/DevOps)
- NCP 인프라 구성 및 관리
- Docker/Docker Compose 설정
- CI/CD 파이프라인 구축
- 환경 변수 및 시크릿 관리
- 로드 밸런싱 및 오토스케일링 설정

### 작업 시 주의사항
- 인프라 변경사항은 반드시 minhan과 협의
- 새로운 환경 변수 추가 시 `.env.example` 업데이트
- 외부 서비스 연동 시 담당자와 사전 협의

## Task Master AI Instructions
**Import Task Master's development workflow commands and guidelines, treat as if import is in the main CLAUDE.md file.**
@./.taskmaster/CLAUDE.md
