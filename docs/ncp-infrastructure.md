# STUDYMATE NCP 인프라 구성 현황

## 📅 문서 정보
- **최종 업데이트**: 2025-08-08
- **작성자**: minhan
- **목적**: NCP 인프라 구성 현황 및 검증 체크리스트

---

## 🌐 1. VPC (Virtual Private Cloud)

### VPC 정보
| 항목 | 값 |
|------|-----|
| **VPC 이름** | live-languagemate |
| **VPC ID** | 115545 |
| **IPv4 CIDR** | 10.10.0.0/16 |
| **Region** | KR-1 |
| **상태** | 운영중 ✅ |

### Subnet 구성

#### Public Subnets

##### 1. public-languagemate-subnet (일반)
| 항목 | 값 |
|------|-----|
| **Subnet 이름** | public-languagemate-subnet |
| **Subnet ID** | 244498 |
| **IPv4 CIDR** | 10.10.10.0/24 |
| **Zone** | KR-1 |
| **Network ACL** | languagemate-public-nacl |
| **Internet Gateway** | Y (Public) |
| **로드밸런서 전용** | N (Normal) |
| **용도** | Bastion Server |

##### 2. public-languagemate-subnet-nat (NAT Gateway용)
| 항목 | 값 |
|------|-----|
| **Subnet 이름** | public-languagemate-subnet-nat |
| **Subnet ID** | 244500 |
| **IPv4 CIDR** | 10.10.11.0/24 |
| **Zone** | KR-1 |
| **Network ACL** | languagemate-public-nacl |
| **Internet Gateway** | Y (Public) |
| **로드밸런서 전용** | N (Normal) |
| **용도** | NAT Gateway |

##### 3. public-languagemate-subnet-lb (Load Balancer용)
| 항목 | 값 |
|------|-----|
| **Subnet 이름** | public-languagemate-subnet-lb |
| **Subnet ID** | 244501 |
| **IPv4 CIDR** | 10.10.13.0/24 |
| **Zone** | KR-1 |
| **Network ACL** | languagemate-public-nacl |
| **Internet Gateway** | Y (Public) |
| **로드밸런서 전용** | Y (Dedicated) |
| **용도** | Load Balancer |

#### Private Subnet
| 항목 | 값 |
|------|-----|
| **Subnet 이름** | private-languagemate-subnet |
| **Subnet ID** | 244499 |
| **IPv4 CIDR** | 10.10.12.0/24 |
| **Zone** | KR-1 |
| **Network ACL** | languagemate-private-nacl |
| **Internet Gateway** | N (Private) |
| **로드밸런서 전용** | N (Normal) |
| **용도** | Database (MySQL, Redis) |

### Gateway
| 항목 | 값 |
|------|-----|
| **Internet Gateway** | 기본 설정 (VPC에 포함) |
| **NAT Gateway 이름** | studymate-nat-gw |
| **NAT Gateway ID** | 107531220 |
| **NAT Gateway 공인 IP** | 175.45.205.226 |
| **NAT Gateway 내부 IP** | 10.10.11.6 |
| **연결 Subnet** | public-languagemate-subnet-nat (10.10.11.0/24) |
| **상태** | 운영중 ✅ |

### 라우팅 테이블

#### Private Route Table
| Route Table | ID | 연결 Subnet | 라우팅 규칙 |
|-------------|-----|------------|------------|
| **live-languagemate-default-private-table** | 239238 | private-languagemate-subnet | 0.0.0.0/0 → NAT Gateway<br>10.10.0.0/16 → LOCAL |

#### Public Route Table  
| Route Table | ID | 연결 Subnet | 라우팅 규칙 |
|-------------|-----|------------|------------|
| **live-languagemate-default-public-table** | 239237 | 3개 Public Subnets | 0.0.0.0/0 → Internet Gateway<br>10.10.0.0/16 → LOCAL |

---

## 🔄 2. Load Balancer

| 항목 | 값 |
|------|-----|
| **이름** | ⚠️ **Load Balancer 미구성** |
| **타입** | - |
| **DNS 이름** | - |
| **공인 IP** | - |
| **리스너** | - |
| **Target Group** | - |
| **헬스체크 경로** | - |

> ⚠️ **주의**: Load Balancer가 구성되지 않아 고가용성 및 부하 분산 불가

---

## 💾 3. Object Storage

| 항목 | 값 |
|------|-----|
| **버킷 이름** | languagemate-profile-img |
| **크기** | 3.24MB |
| **생성일** | 2025-07-30 02:10:32 (UTC+09:00) |
| **접근 권한** | 공개 |
| **용도** | 프로필 이미지 저장 |
| **소유자 권한** | d***********3@naver.com (ncp-3558769-0) |
| **권한** | 목록 조회, 업로드, ACL 조회, ACL 수정 |
| **상태** | 운영중 ✅ |

---

## 🖥️ 4. Server

### Bastion Server
| 항목 | 값 |
|------|-----|
| **서버 이름** | languagemate-bastion |
| **Instance ID** | 107533166 |
| **서버 타입** | s2-g3a (vCPU 2EA, Memory 8GB) |
| **스토리지** | CB2 30GB |
| **OS** | ubuntu-24.04 |
| **내부 IP** | 10.10.10.6 (기본), 10.10.12.6 (추가) |
| **공인 IP** | 223.130.156.72 |
| **Subnet** | public-languagemate-subnet |
| **Zone** | KR-1 |
| **NIC** | eth0, eth1 |
| **상태** | 운영중 ✅ |

### Application Server
| 항목 | 값 |
|------|-----|
| **서버 이름** | ⚠️ **API Server 미확인** |
| **배포 상태** | 확인 필요 |

---

## 🗄️ 5. Cloud DB for MySQL

| 항목 | 값 |
|------|-----|
| **DB 서비스 이름** | languagemate-mysql |
| **DB Server 이름** | languagemate-mysql-001-7nhe |
| **엔진 버전** | MySQL 8.0.42 |
| **인스턴스 타입** | G3 - [Standard] 2vCPU, 8GB Mem |
| **스토리지** | SSD(CB2) 10GB |
| **Private 엔드포인트** | db-36iljh.languagemate.vpc-cdb.ntruss.com |
| **내부 IP** | 10.10.12.7 |
| **포트** | 3306 |
| **VPC** | live-languagemate |
| **Subnet** | private-languagemate-subnet |
| **ACG** | cloud-mysql-1s1j00 (289101) |
| **Multi-AZ** | N |
| **상태** | 운영중 ✅ |

---

## 📮 6. Cloud DB for Redis

| 항목 | 값 |
|------|-----|
| **서비스 이름** | languagemate |
| **DB Server Prefix** | languagemate |
| **엔진 버전** | REDIS 7.2.8 |
| **노드 타입** | G3-1.5GB |
| **메모리** | 1.5GB |
| **Mode** | Simple |
| **Master node 수** | 1 |
| **VPC** | live-languagemate |
| **Subnet** | private-languagemate-subnet |
| **접속 포트** | 6379 |
| **ACG** | cloud-redis-1s1j1e (289105) |
| **DB Config** | redis-simple |
| **상태** | 운영중 ✅ |

---

## 🐳 7. Container Registry

| 항목 | 값 |
|------|-----|
| **레지스트리 이름** | languagemate-server-cr |
| **Public Endpoint** | languagemate-server-cr.kr.ncr.ntruss.com |
| **Private Endpoint** | pns8igdt.kr.private-ncr.ntruss.com |
| **Docker Login** | `docker login languagemate-server-cr.kr.ncr.ntruss.com` |
| **이용 가이드** | [복사] 버튼 사용 가능 |
| **생성일** | 2025-07-27 23:29:19 (UTC+09:00) |
| **상태** | 운영중 ✅ |

---

## 🔒 8. Network ACL

### ⚠️ **심각한 보안 이슈: 모든 Network ACL에 규칙이 없음**

| NACL 이름 | ID | 적용 Subnet | Inbound 규칙 | Outbound 규칙 | 상태 |
|-----------|-----|------------|--------------|---------------|------|  
| **languagemate-public-nacl** | 159112 | 3개 Public Subnet | ❌ 0개 | ❌ 0개 | **트래픽 차단** |
| **languagemate-private-nacl** | 159113 | Private Subnet | ❌ 0개 | ❌ 0개 | **트래픽 차단** |
| **live-languagemate-default-network-acl** | 159110 | 미적용 | ❌ 0개 | ❌ 0개 | 미사용 |

> 🚨 **긴급 조치 필요**: Network ACL에 규칙이 전혀 없어 모든 트래픽이 차단되고 있습니다.
> 이로 인해 인스턴스 간 통신, 외부 접속, 데이터베이스 연결이 모두 불가능한 상태입니다.

### 필요한 Network ACL 규칙 (최소 요구사항)

#### Public NACL (languagemate-public-nacl)
**Inbound:**
- HTTP (80) from 0.0.0.0/0
- HTTPS (443) from 0.0.0.0/0  
- SSH (22) from 관리자 IP
- Ephemeral ports (1024-65535) from 0.0.0.0/0

**Outbound:**
- All traffic to 0.0.0.0/0

#### Private NACL (languagemate-private-nacl)
**Inbound:**
- MySQL (3306) from 10.10.10.0/24
- Redis (6379) from 10.10.10.0/24
- Ephemeral ports from 10.10.0.0/16

**Outbound:**
- HTTPS (443) to 0.0.0.0/0 (외부 API 호출용)
- Ephemeral ports to 10.10.0.0/16

## 🔐 9. ACG (Access Control Group)
> **참고**: ACG는 인스턴스 레벨 보안 그룹으로, 현재 Network ACL이 모든 트래픽을 차단하고 있어 ACG 규칙이 적용되지 않는 상태입니다.

---

## 🌍 10. 도메인 및 DNS

### 도메인 정보
| 항목 | 값 |
|------|-----|
| **도메인** | languagemate.kr |
| **TTL** | 300 |
| **Master NS** | ns1-1.ns-ncloud.com, ns1-2.ns-ncloud.com |

### DNS 레코드
| 호스트 | 타입 | 레코드 값 | TTL |
|--------|------|-----------|-----|
| @ | NS | ns1-1.ns-ncloud.com, ns1-2.ns-ncloud.com | 86400 |
| @ | SOA | ns1-1.ns-ncloud.com | 300 |
| www | CNAME | studymate-client.pages.dev (Cloudflare Pages) | 300 |
| api | A | ⚠️ **미설정** (Load Balancer IP 필요) | - |

---

## 📝 확인 필요 사항 및 이슈

### 🚨 긴급 조치 필요 사항

1. **Network ACL 규칙 없음 (최우선)**
   - 모든 NACL에 인바운드/아웃바운드 규칙이 0개
   - **현재 모든 네트워크 트래픽이 차단된 상태**
   - 서비스 완전 중단 상태로 즉시 규칙 설정 필요

2. **Load Balancer 미구성**
   - Load Balancer 전용 Subnet (public-languagemate-subnet-lb)은 존재하나 LB 미생성
   - 고가용성 및 부하 분산 불가
   - SSL Termination 처리 불가

3. **API Server 미확인**
   - Spring Boot 애플리케이션 서버 위치 불명
   - 현재 Bastion 서버만 확인됨

### ✅ 정상 구성 항목
- VPC 및 Subnet 구성
- NAT Gateway 설정 및 라우팅
- MySQL, Redis 데이터베이스
- Container Registry
- Object Storage
- DNS 기본 설정

### 📡 공인 IP 현황
| IP 주소 | 사용처 | 설명 |
|----------|--------|------|
| **49.50.128.127** | 미할당 | 예비 IP |
| **175.45.205.226** | NAT Gateway | Private Subnet 외부 통신용 |
| **223.130.156.72** | Bastion Server | 관리자 접속용 |

### 📅 마지막 업데이트: 2025-08-08 14:45