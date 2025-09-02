# Docker 배포 성공 - HikariCP 설정 오류 해결

**날짜**: 2025-01-02  
**상태**: ✅ 해결 완료  
**담당자**: minhan (DevOps)

## 🎯 문제 요약

NCP GitHub Actions 워크플로우를 통한 Docker 배포에서 Spring Boot 애플리케이션이 HikariCP "sealed pool" 오류로 인해 시작 실패하는 문제가 발생했습니다.

## 🔍 문제 분석

### 초기 증상
- Docker 컨테이너는 정상 실행되지만 Spring Boot 애플리케이션이 시작 실패
- MySQL 연결은 정상이지만 HikariCP 설정 오류 발생

### 핵심 오류 메시지
```
Property: spring.datasource.hikari.initialization-fail-timeout
Value: "60000"
Origin: class path resource [application.properties] from app.jar - 8:54
Reason: java.lang.IllegalStateException: The configuration of the pool is sealed once started
```

### 근본 원인
`spring.datasource.hikari.initialization-fail-timeout` 속성이 HikariCP 풀이 시작된 후에 설정되려고 시도하여 "sealed pool" 오류 발생. 이 속성은 HikariCP에서 풀이 초기화된 후에는 변경할 수 없는 속성입니다.

## 🛠️ 해결 과정

### 1단계: 문제 속성 식별
- JAR 내부의 `application.properties`에서 `initialization-fail-timeout` 속성 확인
- 해당 속성이 GitHub Actions 워크플로우에서 생성됨을 확인

### 2단계: GitHub Actions 워크플로우 수정
**파일**: `.github/workflows/deploy.yml`

**제거된 라인**:
```properties
spring.datasource.hikari.initialization-fail-timeout=60000
```

**최종 HikariCP 설정**:
```properties
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
```

### 3단계: 배포 및 검증
- 변경사항 커밋 및 푸쉬
- GitHub Actions 자동 배포 실행
- Spring Boot 애플리케이션 정상 시작 확인

## ✅ 해결 결과

### 성공 지표
- ✅ Docker 컨테이너 정상 실행
- ✅ MySQL 연결 성공
- ✅ Redis 연결 성공  
- ✅ Spring Boot 애플리케이션 정상 시작
- ✅ HikariCP 연결 풀 정상 초기화
- ✅ 애플리케이션 Health Check 통과

### 현재 상태
- **배포 환경**: Docker Compose (로컬 DB 사용)
- **데이터베이스**: MySQL 8.0 (Docker 컨테이너)
- **캐시**: Redis 7 (Docker 컨테이너)
- **네트워크**: Bridge 네트워크 (172.20.0.0/16)

## 📚 학습 내용

### HikariCP "Sealed Pool" 개념
- HikariCP 연결 풀은 시작된 후에는 특정 설정을 변경할 수 없음
- `initialization-fail-timeout`은 풀 시작 전에만 설정 가능한 속성
- 런타임에 이러한 속성을 설정하려고 하면 `IllegalStateException` 발생

### 올바른 HikariCP 설정 방법
```properties
# 권장 설정 (문제 없음)
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# 사용하지 않는 것이 좋음 (sealed pool 문제 가능성)
# spring.datasource.hikari.initialization-fail-timeout=60000
```

## 🔧 예방 조치

### 1. HikariCP 설정 검증
- 새로운 HikariCP 속성 추가 시 "sealed" 속성인지 확인
- Spring Boot 공식 문서의 HikariCP 설정 가이드 참조

### 2. 배포 전 로컬 테스트
- Docker Compose로 로컬에서 전체 스택 테스트
- JAR 파일 생성 후 동일한 환경에서 실행 테스트

### 3. 문서화
- 성공/실패 사례를 `docs/99-logs/` 디렉토리에 기록
- HikariCP 설정 가이드를 `docs/07-backend/` 에 추가

## 🎯 향후 개선 사항

### 단기 계획
- [x] HikariCP 설정 오류 해결 
- [ ] 로컬 Docker 환경에서 전체 기능 테스트
- [ ] API 엔드포인트 응답 검증

### 장기 계획
- [ ] NCP 클라우드 서비스로 다시 마이그레이션 고려
- [ ] 모니터링 및 로깅 시스템 구축
- [ ] 자동화된 헬스체크 및 알림 시스템

## 📋 관련 파일

- `.github/workflows/deploy.yml` - GitHub Actions 워크플로우
- `docker-compose.prod.yml` - 프로덕션 Docker 구성
- `src/main/resources/application.properties` - Spring Boot 설정 (JAR 내부)

## 🔗 참고 자료

- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby)
- [Spring Boot HikariCP Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource.hikari)
- [Docker Compose Health Checks](https://docs.docker.com/compose/compose-file/compose-file-v3/#healthcheck)

---

**결론**: `initialization-fail-timeout` 속성 제거로 HikariCP "sealed pool" 오류가 해결되어 Spring Boot 애플리케이션이 정상적으로 배포되었습니다. POC 환경에서 하드코딩 방식으로 안정적인 배포 환경을 구축했습니다.