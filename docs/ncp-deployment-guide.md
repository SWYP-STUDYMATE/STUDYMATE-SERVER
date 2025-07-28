# NCP 배포 가이드 (2025)

## 필요한 NCP 리소스

### 1. 네트워킹
- **VPC**: studymate-vpc (10.0.0.0/16)
- **Public Subnet**: studymate-public-subnet (10.0.1.0/24) - ALB용
- **Private Subnet**: studymate-private-subnet (10.0.2.0/24) - 애플리케이션 서버용
- **Internet Gateway**: 외부 인터넷 연결
- **NAT Gateway**: Private Subnet 아웃바운드 통신

### 2. 데이터베이스
- **Cloud DB for MySQL**: 완전 관리형 MySQL 8.0
  - Multi-AZ 배포로 고가용성 보장
  - 자동 백업 및 Point-in-time Recovery
  - 자동 패치 및 모니터링
- **Cloud DB for Redis**: 완전 관리형 Redis 7
  - Master-Slave 복제 구성
  - 자동 Failover 기능
  - 메모리 최적화 설정

### 3. 컴퓨팅 및 저장소
- **Server (VM)**: 애플리케이션 실행용
- **Container Registry**: Docker 이미지 저장소
- **Object Storage**: Container Registry 백엔드 저장소

### 4. 로드밸런싱 및 보안
- **Application Load Balancer**: L7 로드밸런서
- **Certificate Manager**: SSL 인증서 관리
- **ACG (Access Control Group)**: 방화벽 규칙

## 단계별 구축 가이드

### 1단계: VPC 및 네트워크 설정

```bash
# NCP 콘솔에서 수행
Services > Networking > VPC

1. VPC 생성
   - 이름: studymate-vpc
   - IP CIDR: 10.0.0.0/16

2. Subnet 생성
   - Public Subnet: studymate-public-subnet (10.0.1.0/24)
   - Private Subnet: studymate-private-subnet (10.0.2.0/24)

3. Internet Gateway 생성 및 연결
4. NAT Gateway 생성 (Public Subnet에 배치)
5. Route Table 설정
   - Public: 0.0.0.0/0 → Internet Gateway
   - Private: 0.0.0.0/0 → NAT Gateway
```

### 2단계: 데이터베이스 설정

#### Cloud DB for MySQL
```bash
Services > Database > Cloud DB for MySQL

설정값:
- DB 이름: studymate-mysql
- 버전: MySQL 8.0
- 서버 타입: High Memory (권장)
- 스토리지: SSD 100GB (확장 가능)
- VPC: studymate-vpc
- Subnet: studymate-private-subnet
- Multi-AZ: 활성화 (고가용성)
- 백업: 매일 자동 백업 활성화
```

#### Cloud DB for Redis
```bash
Services > Database > Cloud DB for Redis

설정값:
- 이름: studymate-redis
- 버전: Redis 7.x
- 노드 타입: Standard (2GB 메모리 권장)
- VPC: studymate-vpc
- Subnet: studymate-private-subnet
- 복제: Master-Slave 구성
- 백업: 매일 자동 백업 활성화
```

### 3단계: Container Registry 설정

```bash
Services > Containers > Container Registry

1. Object Storage 생성 (Container Registry 선행 요구사항)
   - 버킷 이름: studymate-container-images

2. Container Registry 생성
   - 이름: studymate-registry
   - 리전: KR-1 (한국)
   - Object Storage 연결
```

### 4단계: Application Load Balancer 설정

```bash
Services > Networking > Load Balancer > Application Load Balancer

설정값:
- 이름: studymate-alb
- Network 타입: Public IP
- VPC: studymate-vpc
- Subnet: studymate-public-subnet (Multi-AZ 권장)
- 성능: Medium (60,000 CPS)
- 리스너: 
  - HTTP (80) → HTTPS (443) 리다이렉트
  - HTTPS (443) → Target Group (8080)
- SSL 인증서: Certificate Manager에서 관리
```

### 5단계: ACG (Access Control Group) 설정

#### ALB ACG
```bash
이름: studymate-alb-acg
규칙:
- Inbound: HTTP (80), HTTPS (443) from 0.0.0.0/0
- Outbound: All traffic to studymate-app-acg
```

#### Application Server ACG
```bash
이름: studymate-app-acg  
규칙:
- Inbound: 
  - 8080 from studymate-alb-acg
  - 22 from 관리자 IP (SSH)
- Outbound: 
  - 3306 to MySQL ACG
  - 6379 to Redis ACG
  - 443 to 0.0.0.0/0 (외부 API 호출)
```

### 6단계: 서버 인스턴스 생성

```bash
Services > Compute > Server

설정값:
- 이름: studymate-app-server
- 이미지: Ubuntu 22.04
- 서버 타입: Standard (2vCPU, 4GB RAM 권장)
- VPC: studymate-vpc
- Subnet: studymate-private-subnet
- ACG: studymate-app-acg
- Storage: SSD 50GB
```

## 환경 변수 설정

### Production 환경 변수
```bash
# 데이터베이스 연결
DB_HOST=<Cloud DB for MySQL 엔드포인트>
DB_PORT=3306
DB_NAME=studymate
DB_USER=<생성한 DB 사용자>
DB_PASSWORD=<DB 비밀번호>

# Redis 연결  
REDIS_HOST=<Cloud DB for Redis 엔드포인트>
REDIS_PORT=6379
REDIS_PASSWORD=<Redis 비밀번호>

# 애플리케이션 설정
JWT_SECRET_KEY=<강력한 비밀키>
NAVER_CLIENT_ID=<네이버 API 클라이언트 ID>
NAVER_CLIENT_SECRET=<네이버 API 클라이언트 시크릿>

# Container Registry
DOCKER_REGISTRY=<registry-name>.kr1.ncr.ntruss.com
APP_IMAGE_NAME=studymate-app
APP_IMAGE_VERSION=latest
```

## 배포 파이프라인

### GitHub Actions CI/CD
```yaml
# .github/workflows/deploy.yml
name: Deploy to NCP

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Build with Gradle
        run: ./gradlew build
        
      - name: Build Docker image
        run: |
          docker build -t ${{ secrets.DOCKER_REGISTRY }}/studymate-app:${{ github.sha }} .
          
      - name: Login to NCP Container Registry
        run: |
          echo ${{ secrets.NCP_SECRET_KEY }} | docker login ${{ secrets.DOCKER_REGISTRY }} -u ${{ secrets.NCP_ACCESS_KEY }} --password-stdin
          
      - name: Push to Container Registry
        run: |
          docker push ${{ secrets.DOCKER_REGISTRY }}/studymate-app:${{ github.sha }}
          
      - name: Deploy to Server
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /app/studymate
            export APP_IMAGE_VERSION=${{ github.sha }}
            docker-compose -f docker-compose.prod.yml pull
            docker-compose -f docker-compose.prod.yml up -d
            docker system prune -f
```

## 모니터링 및 로깅

### Cloud Insight (NCP 모니터링)
```bash
Services > Management > Cloud Insight

설정:
- 서버 모니터링 활성화
- 애플리케이션 성능 모니터링 (APM)
- 로그 수집 및 분석
- 알람 설정 (CPU, 메모리, 디스크 사용률)
```

### 로그 관리
```bash
# 서버에서 로그 디렉토리 설정
/app/studymate/logs/
├── application.log
├── access.log
└── error.log

# 로그 로테이션 설정 (logrotate)
sudo vim /etc/logrotate.d/studymate
```

## 보안 체크리스트

- [ ] ACG 규칙 최소 권한 원칙 적용
- [ ] 데이터베이스 암호화 활성화
- [ ] SSL/TLS 인증서 적용
- [ ] VPC Flow Logs 활성화
- [ ] IAM 사용자 권한 최소화
- [ ] 정기적인 보안 패치 적용
- [ ] 백업 및 복구 절차 수립

## 비용 최적화

1. **예약 인스턴스 사용**: 장기 운영시 비용 절감
2. **Auto Scaling**: 트래픽에 따른 자동 확장/축소
3. **모니터링**: 불필요한 리소스 정기 점검
4. **스토리지 최적화**: 사용하지 않는 스냅샷 정리