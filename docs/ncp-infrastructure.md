# STUDYMATE NCP 인프라 구성 현황

## 📅 문서 정보
- **최종 업데이트**: 2025-08-08
- **작성자**: minhan
- **목적**: NCP 인프라 구성 현황 및 검증 체크리스트

---

## 🌐 1. VPC (Virtual Private Cloud)

### VPC 정보
| 항목          | 값                |
| ------------- | ----------------- |
| **VPC 이름**  | live-languagemate |
| **VPC ID**    | 115545            |
| **IPv4 CIDR** | 10.10.0.0/16      |
| **Region**    | KR-1              |
| **상태**      | 운영중 ✅          |

### Subnet 구성

#### Public Subnets

##### 1. public-languagemate-subnet (일반)
| 항목                 | 값                         |
| -------------------- | -------------------------- |
| **Subnet 이름**      | public-languagemate-subnet |
| **Subnet ID**        | 244498                     |
| **IPv4 CIDR**        | 10.10.10.0/24              |
| **Zone**             | KR-1                       |
| **Network ACL**      | languagemate-public-nacl   |
| **Internet Gateway** | Y (Public)                 |
| **로드밸런서 전용**  | N (Normal)                 |
| **용도**             | Bastion Server             |

##### 2. public-languagemate-subnet-nat (NAT Gateway용)
| 항목                 | 값                             |
| -------------------- | ------------------------------ |
| **Subnet 이름**      | public-languagemate-subnet-nat |
| **Subnet ID**        | 244500                         |
| **IPv4 CIDR**        | 10.10.11.0/24                  |
| **Zone**             | KR-1                           |
| **Network ACL**      | languagemate-public-nacl       |
| **Internet Gateway** | Y (Public)                     |
| **로드밸런서 전용**  | N (Normal)                     |
| **용도**             | NAT Gateway                    |

##### 3. public-languagemate-subnet-lb (Load Balancer용)
| 항목                 | 값                            |
| -------------------- | ----------------------------- |
| **Subnet 이름**      | public-languagemate-subnet-lb |
| **Subnet ID**        | 244501                        |
| **IPv4 CIDR**        | 10.10.13.0/24                 |
| **Zone**             | KR-1                          |
| **Network ACL**      | languagemate-public-nacl      |
| **Internet Gateway** | Y (Public)                    |
| **로드밸런서 전용**  | Y (Dedicated)                 |
| **용도**             | Load Balancer                 |

#### Private Subnet
| 항목                 | 값                          |
| -------------------- | --------------------------- |
| **Subnet 이름**      | private-languagemate-subnet |
| **Subnet ID**        | 244499                      |
| **IPv4 CIDR**        | 10.10.12.0/24               |
| **Zone**             | KR-1                        |
| **Network ACL**      | languagemate-private-nacl   |
| **Internet Gateway** | N (Private)                 |
| **로드밸런서 전용**  | N (Normal)                  |
| **용도**             | ~~Database (MySQL, Redis)~~ → **미사용** |

### Gateway
| 항목                    | 값                                             |
| ----------------------- | ---------------------------------------------- |
| **Internet Gateway**    | 기본 설정 (VPC에 포함)                         |
| **NAT Gateway 이름**    | studymate-nat-gw                               |
| **NAT Gateway ID**      | 107531220                                      |
| **NAT Gateway 공인 IP** | 175.45.205.226                                 |
| **NAT Gateway 내부 IP** | 10.10.11.6                                     |
| **연결 Subnet**         | public-languagemate-subnet-nat (10.10.11.0/24) |
| **상태**                | 운영중 ✅                                       |

### 라우팅 테이블

#### Private Route Table
| Route Table                                 | ID     | 연결 Subnet                 | 라우팅 규칙                                     |
| ------------------------------------------- | ------ | --------------------------- | ----------------------------------------------- |
| **live-languagemate-default-private-table** | 239238 | private-languagemate-subnet | 0.0.0.0/0 → NAT Gateway<br>10.10.0.0/16 → LOCAL |

#### Public Route Table  
| Route Table                                | ID     | 연결 Subnet        | 라우팅 규칙                                          |
| ------------------------------------------ | ------ | ------------------ | ---------------------------------------------------- |
| **live-languagemate-default-public-table** | 239237 | 3개 Public Subnets | 0.0.0.0/0 → Internet Gateway<br>10.10.0.0/16 → LOCAL |

---

## 🔄 2. Load Balancer (미사용)

| 항목              | 값                                                                                                                       |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **상태**          | ❌ 사용 안 함 (Cloudflare Tunnel로 대체)                                                                                  |
| **대체 아키텍처** | Cloudflare DNS + Cloudflare Tunnel → Bastion/Application 서버 직결                                                       |
| **사유**          | 단일 서버 환경에서 LB 제거로 단순화 및 비용 절감. 외부 트래픽은 Cloudflare 엣지에서 종단/보호 후 터널을 통해 내부로 전달 |

> 참고: 고가용성이 필요할 경우 이후 LB 또는 다중 터널/다중 서버 구성을 검토합니다.

---

## 💾 3. Object Storage

| 항목            | 값                                      |
| --------------- | --------------------------------------- |
| **버킷 이름**   | languagemate-profile-img                |
| **크기**        | 3.24MB                                  |
| **생성일**      | 2025-07-30 02:10:32 (UTC+09:00)         |
| **접근 권한**   | 공개                                    |
| **용도**        | 프로필 이미지 저장                      |
| **소유자 권한** | d***********3@naver.com (ncp-3558769-0) |
| **권한**        | 목록 조회, 업로드, ACL 조회, ACL 수정   |
| **상태**        | 운영중 ✅                                |

---

## 🖥️ 4. Server

### Bastion Server
| 항목            | 값                                   |
| --------------- | ------------------------------------ |
| **서버 이름**   | languagemate-bastion                 |
| **Instance ID** | 107533166                            |
| **서버 타입**   | s2-g3a (vCPU 2EA, Memory 8GB)        |
| **스토리지**    | CB2 30GB                             |
| **OS**          | ubuntu-24.04                         |
| **내부 IP**     | 10.10.10.6 (기본), 10.10.12.6 (추가) |
| **공인 IP**     | 223.130.156.72                       |
| **Subnet**      | public-languagemate-subnet           |
| **Zone**        | KR-1                                 |
| **NIC**         | eth0, eth1                           |
| **상태**        | 운영중 ✅                             |

### Application Server
| 항목                  | 값                                       |
| --------------------- | ---------------------------------------- |
| **서버 이름**         | languagemate-bastion (겸용)              |
| **배포 방식**         | 🐳 Docker Compose                       |
| **애플리케이션 포트** | 8080                                     |
| **서비스 구성**       | Spring Boot + MySQL + Redis (모두 컨테이너) |
| **배포 상태**         | ✅ **Docker 기반 운영** (2025-08-26)     |

---

## 🗄️ 5. Database (Docker 기반) ⭐️ **NEW**

### 🐳 Docker Compose 구성
| 항목            | 값                                   |
| --------------- | ------------------------------------ |
| **구성 방식**   | Docker Compose 기반 단일 서버 구성   |
| **MySQL**       | mysql:8.0 (Docker 컨테이너)         |
| **Redis**       | redis:7-alpine (Docker 컨테이너)    |
| **데이터 저장** | Host Volume Mount (`/home/ubuntu/studymate-data`) |
| **백업**        | 자동 백업 컨테이너 (매일 2시)       |
| **상태**        | ✅ 운영중 (2025-08-26부터)           |

### MySQL 컨테이너
| 항목                | 값                              |
| ------------------- | ------------------------------- |
| **컨테이너 이름**   | studymate-mysql                 |
| **이미지**          | mysql:8.0                       |
| **내부 포트**       | 3306                            |
| **데이터 볼륨**     | `/home/ubuntu/studymate-data/mysql` |
| **Character Set**   | utf8mb4                         |
| **Buffer Pool**     | 1GB                             |
| **Max Connections** | 200                             |
| **백업 주기**       | 매일 2시 (7일 보관)             |

### Redis 컨테이너
| 항목              | 값                              |
| ----------------- | ------------------------------- |
| **컨테이너 이름** | studymate-redis                 |
| **이미지**        | redis:7-alpine                  |
| **내부 포트**     | 6379                            |
| **데이터 볼륨**   | `/home/ubuntu/studymate-data/redis` |
| **메모리 제한**   | 512MB                           |
| **Persistence**   | AOF + RDB 백업                  |
| **정책**          | allkeys-lru                     |

### 🔄 이전 구성 (제거됨)
~~Cloud DB for MySQL~~ ❌ **제거 완료 (2025-08-26)**
~~Cloud DB for Redis~~ ❌ **제거 완료 (2025-08-26)**

---

## 🐳 7. Container Registry

| 항목                 | 값                                                      |
| -------------------- | ------------------------------------------------------- |
| **레지스트리 이름**  | languagemate-server-cr                                  |
| **Public Endpoint**  | languagemate-server-cr.kr.ncr.ntruss.com                |
| **Private Endpoint** | pns8igdt.kr.private-ncr.ntruss.com                      |
| **Docker Login**     | `docker login languagemate-server-cr.kr.ncr.ntruss.com` |
| **이용 가이드**      | [복사] 버튼 사용 가능                                   |
| **생성일**           | 2025-07-27 23:29:19 (UTC+09:00)                         |
| **상태**             | 운영중 ✅                                                |

---

## 🔒 8. Network ACL

### ✅ **Network ACL 규칙 설정 완료**

| NACL 이름                                 | ID     | 적용 Subnet       | Inbound 규칙 | Outbound 규칙 | 상태          |
| ----------------------------------------- | ------ | ----------------- | ------------ | ------------- | ------------- |
| **languagemate-public-nacl**              | 159112 | 3개 Public Subnet | ✅ 6개        | ✅ 1개         | **정상 운영** |
| **languagemate-private-nacl**             | 159113 | Private Subnet    | ✅ 3개        | ✅ 2개         | **정상 운영** |
| **live-languagemate-default-network-acl** | 159110 | 미적용            | 0개          | 0개           | 미사용        |

### 설정된 Network ACL 규칙 상세

#### Public NACL (languagemate-public-nacl) - ID: 159112

**Inbound Rules:**
| 우선순위 | 프로토콜 | 접근 소스        | 포트       | 허용여부 | 메모         |
| -------- | -------- | ---------------- | ---------- | -------- | ------------ |
| 100      | TCP      | 0.0.0.0/0 (전체) | 80         | 허용     | https        |
| 101      | TCP      | 0.0.0.0/0 (전체) | 443        | 허용     | https        |
| 102      | TCP      | 165.225.229.3/32 | 22         | 허용     | ssh          |
| 103      | TCP      | 10.10.13.0/24    | 8080       | 허용     | alb-server   |
| 104      | TCP      | 0.0.0.0/0 (전체) | 1024-65535 | 허용     | ephemeral    |
| 105      | TCP      | 10.10.0.0/16     | 1-65535    | 허용     | vpc internal |

**Outbound Rules:**
| 우선순위 | 프로토콜 | 목적지           | 포트    | 허용여부 |
| -------- | -------- | ---------------- | ------- | -------- |
| 100      | TCP      | 0.0.0.0/0 (전체) | 1-65535 | 허용     |

#### Private NACL (languagemate-private-nacl) - ID: 159113

**Inbound Rules:**
| 우선순위 | 프로토콜 | 접근 소스     | 포트    | 허용여부 | 메모         |
| -------- | -------- | ------------- | ------- | -------- | ------------ |
| ~~101~~      | ~~TCP~~      | ~~10.10.10.0/24~~ | ~~3306~~    | ~~허용~~     | ~~mysql~~ ❌ **제거됨**        |
| ~~102~~      | ~~TCP~~      | ~~10.10.10.0/24~~ | ~~6379~~    | ~~허용~~     | ~~redis~~ ❌ **제거됨**        |
| 103      | TCP      | 10.10.0.0/16  | 1-65535 | 허용     | vpc internal |

**Outbound Rules:**
| 우선순위 | 프로토콜 | 목적지           | 포트    | 허용여부 | 메모         |
| -------- | -------- | ---------------- | ------- | -------- | ------------ |
| 101      | TCP      | 0.0.0.0/0 (전체) | 443     | 허용     | https        |
| 102      | TCP      | 10.10.0.0/16     | 1-65535 | 허용     | vpc internal |

## 🔐 9. ACG (Access Control Group)
> **참고**: ACG는 인스턴스 레벨 보안 그룹으로, Network ACL 규칙이 정상 설정되어 ACG 규칙이 정상 적용중입니다.

---

## 🌍 10. 도메인 및 DNS

### 도메인 정보
| 항목          | 값                                       |
| ------------- | ---------------------------------------- |
| **도메인**    | languagemate.kr                          |
| **TTL**       | 300                                      |
| **Master NS** | ns1-1.ns-ncloud.com, ns1-2.ns-ncloud.com |

### DNS 레코드
| 호스트   | 타입  | 레코드 값                                                                              | TTL |
| -------- | ----- | -------------------------------------------------------------------------------------- | --- |
| 네임서버 | NS    | Cloudflare에서 발급된 2개 NS로 변경 (예: ada.ns.cloudflare.com, ben.ns.cloudflare.com) | -   |
| www      | CNAME | studymate-client.pages.dev (Cloudflare Pages)                                          | 300 |
| api      | CNAME | <tunnel-id>.cfargotunnel.com (Cloudflare Tunnel)                                       | 300 |

> 안내: 도메인 `languagemate.kr`의 네임서버를 Cloudflare가 제공한 NS로 변경해야 Cloudflare에서 DNS/보호를 수행합니다. `api`는 Cloudflare Tunnel에 의해 프록시(오렌지 클라우드)됩니다.

---

## 🛡️ 11. SSL 인증서 (Certificate Manager)

### ✅ **SSL 인증서 발급 완료**

| 항목                  | 값                                     |
| --------------------- | -------------------------------------- |
| **인증서 이름**       | languagemate                           |
| **인증서 타입**       | Cloud Basic                            |
| **도메인**            | *.languagemate.kr                      |
| **발급 기관**         | NAVER Secure Certification Authority 1 |
| **발급일**            | 2025-07-28 22:14:32 (UTC+09:00)        |
| **만료일**            | 2026-08-27 20:59:59 (UTC+09:00)        |
| **인증 시작일**       | 2025-07-28 09:00:00 (UTC+09:00)        |
| **Certificate No**    | languagemate (50093)                   |
| **Public Key**        | Sun RSA public key, 2048 bits          |
| **시그니처 알고리즘** | SHA256withRSA                          |
| **검증 방식**         | DNS 검증                               |
| **검증 상태**         | ✅ 성공                                 |
| **갱신 자동화**       | 활성화 (만료 30일 전 자동 갱신)        |
| **상태**              | 정상 ✅                                 |

### DNS 검증 레코드 (완료)
| 도메인            | Record Name                                        | Record Type | 검증 상태 |
| ----------------- | -------------------------------------------------- | ----------- | --------- |
| *.languagemate.kr | _d37ebbbb886f74192a0ae259fc3e4f1e1.languagemate.kr | CNAME       | ✅ 성공    |

### 갱신 대상 설정
| 갱신 대상 여부 | 갱신 자격 | 갱신 시작일            | 갱신 종료일            |
| -------------- | --------- | ---------------------- | ---------------------- |
| Y              | 보적격    | 2026-07-13 (UTC+09:00) | 2026-07-28 (UTC+09:00) |

---

## 📝 확인 필요 사항 및 이슈

### ✅ 완료된 작업

1. **Network ACL 규칙 설정 완료**
   - Public NACL: Inbound 6개, Outbound 1개 규칙 설정
   - Private NACL: Inbound 3개, Outbound 2개 규칙 설정
   - 정상적인 트래픽 흐름 확보

2. **SSL 인증서 발급 완료**
   - Certificate Manager를 통한 와일드카드 인증서 발급
   - DNS 검증 완료
   - 자동 갱신 설정 활성화

### ⏳ 진행 필요 사항 (Cloudflare Tunnel 전환 계획)

1. **Cloudflare DNS 전환**
   - 도메인 `languagemate.kr`의 네임서버를 Cloudflare가 제공한 NS로 변경
   - `api.languagemate.kr` → `<tunnel-id>.cfargotunnel.com` CNAME (프록시 활성화)

2. **서버에 Cloudflare Tunnel 배포**
   - Docker 기반 실행(권장):
     ```bash
     sudo docker run -d --name cloudflared --restart unless-stopped \
       cloudflare/cloudflared:latest \
       tunnel run --no-autoupdate --protocol http2 \
       --token '<CLOUDFLARE_TUNNEL_TOKEN>'
     ```
   - QUIC(UDP 7844) 사용을 원하면 NACL/방화벽에서 UDP 7844 아웃바운드 및 응답 포트를 허용하고 `--protocol auto`(기본) 사용

3. **애플리케이션 컨테이너 실행**
   - `studymate-server` 컨테이너 8080 포트로 실행
   - 터널 Ingress는 `http://localhost:8080`으로 라우팅됨(Cloudflare 대시보드/터널 구성 또는 기본 토큰 모드)

4. **네트워크/보안 정책 조정**
   - Public NACL 인바운드의 80/443 허용 규칙 제거(외부로부터 직접 수신하지 않음)
   - SSH(22)는 관리 IP(165.225.229.3/32)만 허용 유지
   - 아웃바운드 443은 Cloudflare 엣지 접속을 위해 허용 필요(현재 Public NACL Outbound 전체 허용으로 충족)

### ✅ 정상 구성 항목
- VPC 및 Subnet 구성
- NAT Gateway 설정 및 라우팅
- 🐳 **Docker Compose 기반 데이터베이스** (MySQL + Redis)
- Container Registry
- Object Storage
- DNS 기본 설정
- Network ACL 규칙
- SSL 인증서

### 📡 공인 IP 현황
| IP 주소            | 사용처         | 설명                       |
| ------------------ | -------------- | -------------------------- |
| **49.50.128.127**  | 미할당         | 예비 IP                    |
| **175.45.205.226** | NAT Gateway    | Private Subnet 외부 통신용 |
| **223.130.156.72** | Bastion Server | 관리자 접속용              |

### 📅 마지막 업데이트: 2025-08-08 15:30

---

## 📜 변경 이력

### 2025-08-26 (최신)
- 🐳 **Docker Compose 기반 아키텍처로 전환**
  - NCP Cloud DB for MySQL 제거 → Docker 컨테이너로 이전
  - NCP Cloud DB for Redis 제거 → Docker 컨테이너로 이전
  - 단일 서버에서 모든 서비스 통합 운영
  - 자동 백업 시스템 구축 (매일 2시, 7일 보관)
  - GitHub Actions 배포 파이프라인 Docker Compose로 변경

### 2025-08-08 15:30
- ✅ **Network ACL 규칙 설정 완료**
  - languagemate-public-nacl: Inbound 6개, Outbound 1개 규칙 추가
  - languagemate-private-nacl: Inbound 3개, Outbound 2개 규칙 추가 (Database 관련 규칙은 이후 제거)
  - SSH 접근 IP: 165.225.229.3/32로 제한
- ✅ **SSL 인증서 발급 완료**
  - 와일드카드 인증서 (*.languagemate.kr) 발급
  - DNS 검증 완료
  - 자동 갱신 설정 (만료 30일 전)