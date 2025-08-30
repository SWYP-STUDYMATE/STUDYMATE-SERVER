# Studymate Server

스터디메이트 프로젝트의 백엔드 서버입니다.

## ⚠️ 중요 문서 안내

**마스터 PRD**: `../STYDYMATE-CLIENT/.taskmaster/docs/prd.txt`가 전체 STUDYMATE 프로젝트의 **Single Source of Truth**입니다.

**문서 구조**:
- `STUDYMATE-SERVER/docs/` - 백엔드, API, DB, 시스템 아키텍처 문서 (이곳에서 관리)
- `STYDYMATE-CLIENT/docs/` - 프론트엔드 전용 문서 (스타일 가이드, Cloudflare 배포 등)
- `STYDYMATE-CLIENT/.taskmaster/docs/prd.txt` - 전체 프로젝트 마스터 PRD

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [📚 문서 구조](#-문서-구조)
- [기술 스택](#기술-스택)
- [NCP 배포 아키텍처](#ncp-배포-아키텍처)
- [시작하기](#시작하기)

## 프로젝트 소개

STUDYMATE는 실시간 화상통화와 AI 기반 레벨 테스트를 통해 전 세계 사용자들이 언어를 교환하며 학습할 수 있는 플랫폼입니다.

이 저장소는 Spring Boot 기반의 백엔드 API 서버로, 사용자 인증, 채팅, 매칭, 온보딩 등의 핵심 비즈니스 로직을 담당합니다.

## 📚 문서 구조

### 백엔드 관리 문서 (이 프로젝트)
```
STUDYMATE-SERVER/docs/
├── 01-overview/           # 프로젝트 전체 개요
├── 02-requirements/       # 요구사항 (백엔드 관점)
├── 03-architecture/       # 시스템 아키텍처
├── 04-api/               # REST API & WebSocket 명세
├── 05-database/          # DB 스키마, Redis 전략
├── 07-backend/           # 백엔드 서비스, 에러 처리
├── 08-infrastructure/    # NCP 배포, 인프라
├── 09-processes/         # 개발 워크플로우
├── 10-decisions/         # 기술 스택 결정사항
└── 99-logs/              # 작업 로그 (실제 세션 3개)
```

### 클라이언트 전용 문서 (STYDYMATE-CLIENT)
```
STYDYMATE-CLIENT/docs/
├── 06-frontend/          # React 컴포넌트, 스타일 가이드
├── 08-infrastructure/    # Cloudflare Pages 배포
└── 99-logs/              # 클라이언트 작업 로그
```

### 마스터 문서
- **[전체 프로젝트 PRD](../STYDYMATE-CLIENT/.taskmaster/docs/prd.txt)** - 252줄의 상세한 전체 요구사항

## ⚠️ 서버 개발 시 필수 참조 문서

**백엔드 개발자는 다음 클라이언트 문서를 반드시 확인해야 합니다:**

### 🎨 프론트엔드 연동 필수
- **[스타일 가이드](../STYDYMATE-CLIENT/docs/06-frontend/style-guide.md)** - UI 컴포넌트 디자인 규격 (API 응답 형식에 영향)
- **[React 컴포넌트](../STYDYMATE-CLIENT/docs/06-frontend/components/README.md)** - 클라이언트 컴포넌트 구조 이해

### ⚙️ Node.js Workers 연동 
- **[TaskMaster PRD](../STYDYMATE-CLIENT/.taskmaster/docs/prd.txt)** - Section 3: Node.js 백엔드 개발 태스크 (119-189줄)
  - WebRTC 시그널링 서버 연동
  - AI 레벨테스트 API 연동  
  - Cloudflare Workers/Durable Objects 연동

### 🔗 필수 체크 사항
- **API 응답 형식**: 클라이언트 컴포넌트가 기대하는 데이터 구조 확인
- **WebSocket 이벤트**: 클라이언트에서 처리하는 이벤트 타입 확인
- **에러 처리**: 클라이언트 에러 핸들링과 일치하는 에러 코드 사용

## 📝 문서 업데이트 필수 규칙

### ⚠️ 백엔드 개발자 절대 원칙
1. **API 변경 = 문서 업데이트 필수**
   - Controller 변경 → `docs/04-api/api-reference.md` 업데이트
   - DTO 변경 → 클라이언트 TypeScript 인터페이스 확인 요청  
   - Entity 변경 → `docs/05-database/database-schema.md` 업데이트
   - 에러 코드 추가 → `docs/07-backend/error-handling.md` 업데이트

2. **클라이언트 연동 확인 의무**
   - API 응답 변경 시 → 클라이언트 컴포넌트 요구사항 확인
   - WebSocket 이벤트 변경 시 → 클라이언트 이벤트 핸들러 확인
   - 에러 응답 변경 시 → 클라이언트 에러 처리 로직 확인

3. **상호 참조 문서 동기화**
   - `src/*/dto/response/` ↔ 클라이언트 TypeScript interface  
   - WebSocket 이벤트 ↔ 클라이언트 이벤트 리스너
   - 에러 코드 ↔ 클라이언트 에러 메시지

### 🔄 백엔드 업데이트 워크플로우
```bash
# 1. API 변경 전 클라이언트 요구사항 확인
ls ../STYDYMATE-CLIENT/src/types/  # TypeScript 인터페이스 확인
cat ../STYDYMATE-CLIENT/docs/06-frontend/components/README.md

# 2. 백엔드 개발 진행  
# ... Controller, Service, DTO 작성 ...

# 3. 문서 업데이트 (필수!)
# - docs/04-api/api-reference.md (API 명세)
# - docs/07-backend/error-handling.md (에러 코드)  
# - docs/05-database/ (DB 변경시)

# 4. 클라이언트 팀에 변경사항 알림
git add . && git commit -m "feat: API 기능명
- 백엔드 변경 내용  
- 📝 API 문서 업데이트 완료
- ⚠️ 클라이언트 TypeScript 인터페이스 확인 필요"
```

### 🔗 크로스 프로젝트 확인 체크리스트  
- [ ] DTO Response 변경 시 클라이언트 TypeScript interface 일치 확인
- [ ] API 엔드포인트 변경 시 클라이언트 API 호출 코드 확인
- [ ] WebSocket 이벤트 변경 시 클라이언트 이벤트 핸들러 확인
- [ ] 에러 코드 변경 시 클라이언트 에러 처리 로직 확인  
- [ ] 상태 값 변경 시 클라이언트 상태 관리 store 확인

## 기술 스택

- **Language:** Java 17
- **Framework:** Spring Boot 3
- **Database:** MySQL
- **Cache:** Redis
- **Build Tool:** Gradle

---

## NCP 배포 아키텍처

이 프로젝트는 Naver Cloud Platform(NCP)에 고가용성 및 보안을 고려하여 배포됩니다.

### 아키텍처 다이어그램

```
+---------------------------------------------------------------------------------+
|                                  Internet                                       |
+----------------------------------+----------------------------------------------+
                                   |
                                   v
+--------------------------+---------------------------+
|     languagemate.kr      |      DNS (NCP)            |
+--------------------------+---------------------------+
                                   |
                                   v
+---------------------------------------------------------------------------------+
|                            VPC (studymate-vpc) 10.0.0.0/16                        |
|                                                                                 |
| +-----------------------------------------------------------------------------+ |
| | Public Subnet (studymate-public-subnet) 10.0.1.0/24                         | |
| |                                                                             | |
| |   +-------------------------+      +------------------------------------+   | |
| |   |   Internet Gateway      |----->|  Load Balancer (studymate-lb)      |   | |
| |   +-------------------------+      |   - Public IP                      |   | |
| |                                    |   - ACG: studymate-lb-acg          |   | |
| |                                    |   - NACL: studymate-public-nacl    |   | |
| |                                    |   - SSL/TLS Offloading             |   | |
| |                                    +------------------+-----------------+   | |
| +-------------------------------------------------------|---------------------+ |
|                                                         | (HTTPS/443 -> HTTP/8080)
|                                                         v
| +-----------------------------------------------------------------------------+ |
| | Private Subnet (studymate-private-subnet) 10.0.2.0/24                       | |
| |                                                                             | |
| |   +-------------------------+      +------------------------------------+   | |
| |   |    NAT Gateway          |<---->|  Server Instance (VM)              |   | |
| |   |   (Outbound Traffic)    |      |   - Private IP                     |   | |
| |   +-------------------------+      |   - ACG: studymate-server-acg      |   | |
| |                                    |   - NACL: studymate-private-nacl   |   | |
| |                                    |                                    |   | |
| |                                    |   +------------------------------+ |   | |
| |                                    |   | Docker Engine                | |   | |
| |                                    |   |  - studymate-app (Java)      | |   | |
| |                                    |   |  - studymate-db (MySQL)      | |   | |
| |                                    |   |  - studymate-redis (Redis)   | |   | |
| |                                    |   +------------------------------+ |   | |
| |                                    +------------------------------------+   | |
| +-----------------------------------------------------------------------------+ |
|                                                                                 |
+---------------------------------------------------------------------------------+
```

### 네트워크 구성

-   **VPC (`studymate-vpc`):** `10.0.0.0/16` 대역을 사용하는 격리된 가상 네트워크입니다.
-   **Public Subnet (`studymate-public-subnet`):** `10.0.1.0/24` 대역을 사용하며, 외부 인터넷과 직접 통신하는 리소스(로드밸런서)가 위치합니다.
    -   **Internet Gateway:** VPC와 외부 인터넷 간의 통신을 담당합니다.
-   **Private Subnet (`studymate-private-subnet`):** `10.0.2.0/24` 대역을 사용하며, 외부에서 직접 접근할 수 없는 내부 리소스(애플리케이션 서버)가 위치합니다.
    -   **NAT Gateway:** Private Subnet의 서버가 외부 인터넷(예: GitHub, 외부 API)으로 나가는 아웃바운드 통신을 위해 사용됩니다.

### 보안 설정

-   **Network ACL (NACL):** 서브넷 레벨의 방화벽으로, Stateless 방식으로 동작합니다.
    -   `studymate-public-nacl`: Public Subnet에 적용되며, HTTP(80), HTTPS(443) 트래픽만 허용합니다.
    -   `studymate-private-nacl`: Private Subnet에 적용되며, 로드밸런서로부터의 8080 포트 트래픽과 관리자 SSH 접속 등 최소한의 트래픽만 허용합니다.
-   **ACG (Access Control Group):** 서버 인스턴스 레벨의 방화벽으로, Stateful 방식으로 동작합니다.
    -   `studymate-lb-acg`: 로드밸런서에 적용되며, HTTP(80), HTTPS(443) 트래픽을 허용합니다.
    -   `studymate-server-acg`: 서버 인스턴스에 적용되며, 로드밸런서가 속한 Public Subnet 대역에서의 8080 포트 접근과 관리자 IP에서의 SSH(22) 접근만 허용합니다.

### 서버 및 배포

-   **서버 인스턴스:** Private Subnet에 위치하며, 외부에서 직접 접근이 불가능합니다.
-   **로드밸런서:** Application Load Balancer를 사용하여 `languagemate.kr` 도메인의 HTTPS 요청을 받아 서버의 8080 포트로 전달합니다. SSL 인증서 관리 및 암호화/복호화(SSL Offloading)를 담당합니다.
-   **배포 방식:**
    -   GitHub Actions를 통해 CI/CD 파이프라인을 구축합니다.
    -   애플리케이션, MySQL, Redis는 서버 인스턴스 내의 Docker 컨테이너로 실행되며, `docker-compose`를 통해 관리됩니다.
    -   GitHub Actions는 코드를 빌드하고 Docker 이미지를 생성하여 NCP Container Registry에 푸시한 후, SSH를 통해 서버에 접속하여 최신 이미지를 받아 서비스를 재시작하는 방식으로 배포를 자동화합니다.

## 시작하기

(여기에 로컬 환경에서 프로젝트를 실행하는 방법을 추가해주세요.)
