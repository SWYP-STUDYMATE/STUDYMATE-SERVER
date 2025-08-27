# 🔄 개발 워크플로우

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Development Team
- **목적**: STUDYMATE 프로젝트의 개발 프로세스 및 워크플로우 정의

---

## 🎯 개발 프로세스 개요

### 개발 철학
1. **품질 우선**: 코드 품질과 안정성을 최우선으로 함
2. **협업 중심**: 투명한 커뮤니케이션과 코드 리뷰
3. **자동화**: 반복 작업의 자동화를 통한 효율성 증대
4. **지속적 개선**: 회고와 피드백을 통한 지속적 프로세스 개선

### 팀 구성 및 역할
```yaml
Backend Developer A:
  - Spring Boot 애플리케이션 개발
  - 비즈니스 로직 구현
  - API 설계 및 구현
  - 데이터베이스 스키마 설계

minhan (DevOps/Infrastructure):
  - NCP 인프라 구성 및 관리
  - Docker/Docker Compose 설정
  - CI/CD 파이프라인 구축
  - 모니터링 및 로깅 시스템 관리
```

---

## 📋 Git 워크플로우

### 브랜치 전략 (Git Flow 변형)
```
main (production)
├── develop (통합 개발)
├── feature/[task-id]-[description] (기능 개발)
├── hotfix/[issue-id]-[description] (긴급 수정)
└── release/[version] (릴리스 준비)
```

### 브랜치 규칙

#### Main 브랜치
- **목적**: 프로덕션 배포용 안정화된 코드
- **보호 정책**: 
  - 직접 푸시 금지
  - Pull Request를 통해서만 병합
  - 최소 1명의 리뷰어 승인 필요
- **자동 배포**: main 브랜치 커밋 시 프로덕션 자동 배포

#### Develop 브랜치
- **목적**: 개발 브랜치들의 통합 및 테스트
- **특징**: 
  - 모든 feature 브랜치의 병합 대상
  - 개발 환경에서 테스트
  - 릴리스 준비가 완료되면 main으로 병합

#### Feature 브랜치
- **명명 규칙**: `feature/[issue-number]-[brief-description]`
- **예시**: 
  - `feature/123-user-authentication`
  - `feature/456-websocket-chat`
  - `feature/789-level-test-api`
- **수명**: 개발 시작부터 develop 병합까지

#### Hotfix 브랜치
- **명명 규칙**: `hotfix/[issue-number]-[brief-description]`
- **예시**: `hotfix/999-critical-login-bug`
- **특징**: main에서 분기하여 긴급 수정 후 main과 develop 동시 병합

---

## 🛠️ 개발 프로세스

### 1. 이슈 생성 및 계획
```yaml
Step 1: GitHub Issues에서 작업 아이템 생성
  - Title: 명확하고 간결한 제목
  - Description: 상세한 요구사항과 수용 기준
  - Labels: bug, feature, enhancement, etc.
  - Assignee: 담당자 지정
  - Milestone: 연결된 릴리스 마일스톤

Step 2: Issue 템플릿 활용
  - Bug Report Template
  - Feature Request Template
  - Task Template
```

#### Issue 템플릿 예시
```markdown
## 📋 작업 설명
간단하고 명확한 작업 설명

## 🎯 수용 기준
- [ ] 기준 1
- [ ] 기준 2
- [ ] 기준 3

## 📝 상세 요구사항
구체적인 구현 요구사항

## 🔗 관련 이슈
- Related to #123
- Depends on #456

## 📋 체크리스트
- [ ] 코드 구현 완료
- [ ] 테스트 작성 완료
- [ ] 문서 업데이트 완료
- [ ] 코드 리뷰 완료
```

### 2. 브랜치 생성 및 개발
```bash
# 최신 develop 브랜치에서 시작
git checkout develop
git pull origin develop

# 새로운 feature 브랜치 생성
git checkout -b feature/123-user-authentication

# 개발 진행
# ... 코딩 작업 ...

# 커밋 (Conventional Commits 규칙 준수)
git add .
git commit -m "feat(auth): implement user authentication with JWT tokens

- Add JWT token generation and validation
- Implement login/logout endpoints
- Add security configuration for protected routes
- Include unit tests for authentication service

Resolves #123"

# 원격 브랜치에 푸시
git push origin feature/123-user-authentication
```

### 3. Pull Request 생성
```yaml
PR 작성 가이드라인:
  Title: "[Feature] User Authentication System"
  Description:
    - 작업 내용 요약
    - 변경 사항 상세 설명
    - 테스트 방법
    - 스크린샷 (UI 변경사항 있는 경우)
  
  Reviewers: 
    - Backend Developer A (필수)
    - minhan (인프라 관련 변경사항 있는 경우)

  Labels:
    - feature/bug/enhancement
    - backend/frontend/infrastructure
    - priority-high/medium/low
```

#### PR 템플릿
```markdown
## 🎯 작업 요약
이 PR에서 수행한 작업에 대한 간단한 설명

## 📋 변경 사항
- [ ] 새로운 기능 추가
- [ ] 버그 수정
- [ ] 리팩토링
- [ ] 문서 업데이트
- [ ] 설정 변경

### 상세 변경 내용
- JWT 인증 시스템 구현
- User 엔티티 및 Repository 추가
- Security Configuration 설정

## 🧪 테스트
### 테스트 방법
1. 회원가입 API 호출
2. 로그인 API 호출하여 JWT 토큰 확인
3. 보호된 엔드포인트 접근 테스트

### 테스트 결과
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 수동 테스트 완료

## 📸 스크린샷 (선택사항)
UI 변경사항이 있는 경우 스크린샷 첨부

## 🔗 관련 이슈
- Resolves #123
- Related to #456

## ⚠️ 주의사항
배포 시 고려해야 할 사항이나 설정 변경 필요사항

## 📋 리뷰 체크리스트
리뷰어를 위한 체크리스트
- [ ] 코드가 요구사항을 충족하는가?
- [ ] 테스트 커버리지가 충분한가?
- [ ] 성능상 문제는 없는가?
- [ ] 보안 취약점은 없는가?
```

### 4. 코드 리뷰 프로세스
```yaml
리뷰 기준:
  Code Quality:
    - 클린 코드 원칙 준수
    - 적절한 네이밍 컨벤션
    - 중복 코드 제거
    - 적절한 주석과 문서화
  
  Architecture:
    - 레이어 아키텍처 준수
    - SOLID 원칙 적용
    - 적절한 디자인 패턴 사용
  
  Security:
    - SQL 인젝션 방지
    - XSS 방지
    - 인증/인가 적절한 구현
    - 민감 정보 노출 방지
  
  Performance:
    - 효율적인 쿼리 작성
    - 적절한 캐싱 전략
    - 리소스 사용 최적화

리뷰 프로세스:
  1. 자동 테스트 통과 확인
  2. 코드 품질 자동 검사 (SonarQube 예정)
  3. 수동 코드 리뷰 진행
  4. 피드백 반영 및 수정
  5. 승인 후 병합
```

### 5. 병합 및 배포
```bash
# PR 승인 후 develop 브랜치로 병합 (GitHub UI에서)
# Squash and merge 권장

# develop에서 main으로 병합 (릴리스 시)
git checkout main
git pull origin main
git merge develop
git push origin main

# main 푸시 시 자동 배포 실행 (GitHub Actions)
```

---

## 📝 커밋 메시지 규칙

### Conventional Commits 적용
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 커밋 타입
```yaml
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경 (포매팅, 세미콜론 누락 등)
refactor: 코드 리팩토링
test: 테스트 코드 추가/수정
chore: 빌드 프로세스 또는 보조 도구 변경
perf: 성능 개선
ci: CI/CD 설정 변경
```

### 커밋 메시지 예시
```bash
# 좋은 커밋 메시지
git commit -m "feat(auth): add JWT token refresh functionality

- Implement refresh token rotation
- Add token expiry validation
- Include security headers for token endpoints
- Add unit tests for token refresh logic

Resolves #456"

# 나쁜 커밋 메시지
git commit -m "fix bug"
git commit -m "update code"
git commit -m "changes"
```

---

## 🧪 테스트 전략

### 테스트 피라미드
```
    🔺
   /UI\     (E2E Tests - 최소)
  /────\    
 /Integ\    (Integration Tests - 적당)
/ration\
────────    (Unit Tests - 대부분)
```

### 테스트 유형별 가이드라인

#### 단위 테스트 (Unit Tests)
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("사용자 생성 시 정상적으로 저장되어야 한다")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserDto createDto = CreateUserDto.builder()
            .naverId("naver123")
            .englishName("John Doe")
            .build();
        
        User savedUser = User.builder()
            .id(1L)
            .naverId("naver123")
            .englishName("John Doe")
            .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserDto result = userService.createUser(createDto);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNaverId()).isEqualTo("naver123");
        assertThat(result.getEnglishName()).isEqualTo("John Doe");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

#### 통합 테스트 (Integration Tests)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test_studymate")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("사용자 프로필 조회 API 통합 테스트")
    void shouldGetUserProfile() {
        // Given
        String jwtToken = "valid_jwt_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<UserDto> response = restTemplate.exchange(
            "/api/v1/users/profile", 
            HttpMethod.GET, 
            entity, 
            UserDto.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
```

#### E2E 테스트 (End-to-End Tests)
```java
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AuthenticationE2ETest {
    
    private WebDriver driver;
    
    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }
    
    @Test
    @DisplayName("Naver 로그인 전체 플로우 테스트")
    void shouldCompleteNaverLoginFlow() {
        // Given
        driver.get("http://localhost:3000/login");
        
        // When
        WebElement naverLoginButton = driver.findElement(By.id("naver-login"));
        naverLoginButton.click();
        
        // 실제 Naver OAuth 플로우는 테스트 환경에서 모킹
        // ...
        
        // Then
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        assertThat(driver.getCurrentUrl()).contains("/dashboard");
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

### 테스트 실행 전략
```yaml
Local Development:
  - 단위 테스트: 매번 빌드 시 실행
  - 통합 테스트: 개발자 판단에 따라 실행
  - E2E 테스트: 주요 변경사항 있을 때만 실행

CI/CD Pipeline:
  - PR 생성 시: 단위 테스트 + 통합 테스트
  - main 브랜치 푸시: 전체 테스트 스위트 실행
  - 배포 전: E2E 테스트 포함 전체 테스트
```

---

## 🔧 코드 품질 관리

### 정적 분석 도구
```yaml
Java Code Quality:
  - Checkstyle: 코딩 스타일 검사
  - PMD: 코드 품질 분석
  - SpotBugs: 버그 패턴 검출
  - SonarQube: 종합적인 코드 품질 분석 (예정)

Build Tools:
  - Gradle: 빌드 자동화
  - JaCoCo: 테스트 커버리지 측정
```

### 코드 커버리지 기준
```yaml
Target Coverage:
  - Line Coverage: 80% 이상
  - Branch Coverage: 75% 이상
  - Method Coverage: 90% 이상

Exclusions:
  - Configuration 클래스
  - DTO 클래스
  - Entity 클래스 (Getter/Setter)
  - Test 클래스
```

### Gradle 설정 예시
```gradle
// build.gradle
plugins {
    id 'jacoco'
    id 'checkstyle'
    id 'pmd'
    id 'com.github.spotbugs' version '5.0.13'
}

jacoco {
    toolVersion = "0.8.8"
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir('jacocoHtml')
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80
            }
        }
    }
}

checkstyle {
    toolVersion = '10.3.4'
    configFile = file('config/checkstyle/checkstyle.xml')
}

pmd {
    toolVersion = '6.48.0'
    ruleSets = []
    ruleSetFiles = files('config/pmd/pmd-rules.xml')
}
```

---

## 🚀 릴리스 프로세스

### 릴리스 계획
```yaml
릴리스 주기: 2주마다 (Bi-weekly)

릴리스 타입:
  - Major (x.0.0): 큰 기능 변경, Breaking Changes
  - Minor (x.y.0): 새로운 기능 추가, 하위 호환성 유지
  - Patch (x.y.z): 버그 수정, 보안 패치

릴리스 네이밍:
  - Version: Semantic Versioning (v1.2.3)
  - Tag: vMAJOR.MINOR.PATCH (v1.2.3)
  - Release Name: 계절/테마 기반 (예: Spring Breeze, Summer Wave)
```

### 릴리스 프로세스 단계
```yaml
1. 릴리스 계획 (Release Planning):
   - 마일스톤 생성 및 이슈 할당
   - 기능 우선순위 결정
   - 릴리스 일정 수립

2. 개발 단계 (Development Phase):
   - Feature 브랜치에서 개발
   - 개별 기능 테스트 및 리뷰
   - Develop 브랜치로 점진적 통합

3. 릴리스 준비 (Release Preparation):
   - Release 브랜치 생성
   - 통합 테스트 수행
   - 문서 업데이트
   - 릴리스 노트 작성

4. 릴리스 배포 (Release Deployment):
   - Main 브랜치로 병합
   - 프로덕션 배포
   - 모니터링 및 검증

5. 릴리스 후 (Post-Release):
   - 핫픽스 대응 준비
   - 사용자 피드백 수집
   - 다음 릴리스 계획 수립
```

### 릴리스 체크리스트
```markdown
## 릴리스 체크리스트 v1.2.3

### 개발 완료 확인
- [ ] 모든 feature 브랜치가 develop에 병합 완료
- [ ] 모든 테스트 통과 (Unit, Integration, E2E)
- [ ] 코드 리뷰 완료
- [ ] 성능 테스트 완료

### 문서 업데이트
- [ ] API 문서 업데이트 (Swagger)
- [ ] README.md 업데이트
- [ ] 릴리스 노트 작성
- [ ] 마이그레이션 가이드 작성 (Breaking Changes 있는 경우)

### 보안 및 품질 검사
- [ ] 보안 취약점 스캔 완료
- [ ] 코드 품질 분석 통과
- [ ] 의존성 라이브러리 업데이트 확인

### 배포 준비
- [ ] 환경 변수 설정 확인
- [ ] 데이터베이스 마이그레이션 스크립트 준비
- [ ] 백업 절차 확인
- [ ] 롤백 계획 수립

### 배포 후 검증
- [ ] 헬스 체크 통과
- [ ] 주요 기능 동작 확인
- [ ] 모니터링 알람 설정 확인
- [ ] 사용자 공지사항 발송
```

---

## 📊 개발 메트릭 및 모니터링

### 개발 생산성 지표
```yaml
Velocity Metrics:
  - Story Points per Sprint
  - Lead Time (Issue 생성 → 배포)
  - Cycle Time (개발 시작 → 배포)
  - Deployment Frequency

Quality Metrics:
  - Bug Density (버그 수 / 코드 라인)
  - Test Coverage
  - Code Review Coverage
  - Defect Escape Rate

Collaboration Metrics:
  - PR Size (변경된 라인 수)
  - PR Review Time
  - PR Rejection Rate
  - Code Review Comments per PR
```

### 개발 대시보드
```yaml
GitHub Insights 활용:
  - Contributor Activity
  - Code Frequency
  - Pull Request Trends
  - Issue Resolution Time

Custom Metrics (향후 추가 예정):
  - Grafana Dashboard
  - Development KPI Tracking
  - Automated Reporting
```

---

## 🎯 베스트 프랙티스

### 코드 작성 가이드라인
```yaml
General Principles:
  - KISS (Keep It Simple, Stupid)
  - DRY (Don't Repeat Yourself)
  - YAGNI (You Aren't Gonna Need It)
  - SOLID 원칙 준수

Java Specific:
  - Optional 적극 활용
  - Stream API 사용 권장
  - 의존성 주입 생성자 방식 사용
  - @Transactional 적절한 사용

Spring Boot Specific:
  - @RestController vs @Controller 명확한 구분
  - ResponseEntity 적절한 사용
  - Custom Exception 정의 및 활용
  - Validation 애노테이션 활용
```

### 문서화 가이드라인
```yaml
Code Documentation:
  - Public API에 Javadoc 작성
  - 복잡한 비즈니스 로직에 주석
  - README 파일 최신 상태 유지

Architecture Documentation:
  - 중요한 설계 결정 문서화
  - ADR (Architecture Decision Record) 작성
  - API 설계 가이드라인 문서화
```

### 보안 가이드라인
```yaml
Secure Coding:
  - 사용자 입력 검증 철저히
  - SQL Injection 방지
  - XSS 공격 방지
  - CSRF 토큰 적절한 사용

Dependency Management:
  - 정기적인 라이브러리 업데이트
  - 보안 취약점 스캔
  - Least Privilege 원칙 적용
```

---

## 🔄 프로세스 개선

### 회고 프로세스
```yaml
정기 회고:
  - Sprint Retrospective (2주마다)
  - 월간 프로세스 개선 회의
  - 분기별 기술 회고

회고 형식:
  - Keep (계속할 것)
  - Problem (문제점)
  - Try (시도할 것)

액션 아이템:
  - 구체적이고 측정 가능한 목표
  - 담당자 및 완료 기한 명시
  - 다음 회고에서 진행상황 점검
```

### 지속적 개선 영역
```yaml
Process Improvements:
  - 개발 도구 및 환경 개선
  - 자동화 확대 (테스트, 배포, 모니터링)
  - 코드 품질 도구 도입

Team Collaboration:
  - 페어 프로그래밍 도입 검토
  - 기술 공유 세션 정기 개최
  - 외부 컨퍼런스 참가 및 지식 공유
```

---

## 📚 관련 문서

- [기능 요구사항](../02-requirements/functional-requirements.md)
- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [API 레퍼런스](../04-api/api-reference.md)
- [배포 가이드](../08-infrastructure/deployment-guide.md)
- [에러 처리 가이드](../07-backend/error-handling.md)

---

## 📞 문의 및 지원

### 개발 관련 문의
- **GitHub Issues**: 버그 리포트 및 기능 요청
- **Pull Request**: 코드 리뷰 및 피드백
- **팀 미팅**: 매주 화요일 오후 2시

### 도구 및 리소스
- **코드 저장소**: [GitHub Repository](https://github.com/SWYP-STUDYMATE/STUDYMATE-SERVER)
- **프로젝트 관리**: GitHub Projects & Issues
- **커뮤니케이션**: Slack #dev-studymate
- **문서 관리**: GitHub Wiki & docs/ 폴더