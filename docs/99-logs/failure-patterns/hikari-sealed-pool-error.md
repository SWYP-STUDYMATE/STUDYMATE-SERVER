# HikariCP "Sealed Pool" 오류 패턴

## 🚨 오류 패턴 식별

**오류 유형**: Configuration Error  
**심각도**: High (애플리케이션 시작 불가)  
**발생 빈도**: 일회성 (설정 오류)

## 📋 증상

### 오류 메시지
```
java.lang.IllegalStateException: The configuration of the pool is sealed once started
Property: spring.datasource.hikari.initialization-fail-timeout
Value: "60000"
Origin: class path resource [application.properties] from app.jar - 8:54
```

### 동반 증상
- Spring Boot 애플리케이션 시작 실패
- Docker 컨테이너는 실행되지만 애플리케이션 프로세스가 종료됨
- 데이터베이스 연결은 정상이지만 HikariCP 초기화 단계에서 실패

## 🔍 근본 원인

### HikariCP "Sealed Pool" 개념
HikariCP는 성능 최적화를 위해 연결 풀이 시작된 후 특정 설정을 변경할 수 없도록 "seal" 처리합니다.

### 문제가 되는 속성들
```properties
# 🚫 Sealed 후 설정 불가능한 속성들
spring.datasource.hikari.initialization-fail-timeout
spring.datasource.hikari.pool-name  
spring.datasource.hikari.data-source-class-name
```

### 발생 시나리오
1. Spring Boot 애플리케이션 시작
2. HikariCP 연결 풀 초기화 및 "seal" 처리
3. 추가 설정에서 `initialization-fail-timeout` 설정 시도
4. `IllegalStateException` 발생으로 애플리케이션 종료

## ✅ 해결 방법

### 즉시 해결책
**문제 속성 제거**:
```yaml
# .github/workflows/deploy.yml 에서 제거
# spring.datasource.hikari.initialization-fail-timeout=60000
```

### 권장 HikariCP 설정
```properties
# ✅ 안전한 HikariCP 설정
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# ✅ 런타임 중 변경 가능한 속성들
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

## 🚫 예방 조치

### 1. HikariCP 설정 검증 체크리스트
- [ ] 새로운 HikariCP 속성 추가 시 "sealed" 속성인지 확인
- [ ] Spring Boot 공식 문서의 HikariCP 가이드 참조
- [ ] 로컬 환경에서 JAR 파일 생성 후 테스트

### 2. 안전한 개발 워크플로우
```bash
# 로컬 테스트 절차
./gradlew bootJar
java -jar build/libs/app.jar
# 정상 시작 확인 후 배포
```

### 3. 문서화
- HikariCP 설정 가이드를 백엔드 문서에 추가
- 금지된 속성 목록을 명시적으로 관리

## 🔗 관련 자료

### HikariCP 공식 문서
- [HikariCP Configuration Guide](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby)
- [Spring Boot HikariCP Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource.hikari)

### 관련 이슈
- GitHub Issue: Configuration sealed after pool start
- Stack Overflow: HikariCP IllegalStateException

## 📊 비슷한 오류 패턴

### 관련 Configuration 오류들
1. **DataSource Configuration Sealed**: 데이터소스 설정이 봉인된 후 변경 시도
2. **Connection Pool State Error**: 풀 상태와 맞지 않는 설정 시도  
3. **Property Override Conflict**: 여러 소스에서 동일 속성을 다른 값으로 설정

### 예방을 위한 일반 원칙
```properties
# 원칙 1: 풀 시작 전에만 설정 가능한 속성은 초기 설정에서만 사용
# 원칙 2: 런타임 변경이 필요한 설정은 관리형 속성 사용
# 원칙 3: 설정 소스 간 충돌 방지 (application.properties vs 환경변수)
```

---

**해결 완료일**: 2025-01-02  
**해결자**: minhan (DevOps)  
**상태**: ✅ 해결됨 및 예방 조치 완료