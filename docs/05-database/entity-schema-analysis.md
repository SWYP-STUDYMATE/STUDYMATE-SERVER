# 📊 엔티티 스키마 분석 리포트

## 📅 문서 정보
- **생성 일시**: 2025-09-10
- **작성자**: Backend Development Team
- **목적**: STUDYMATE 엔티티 스키마 정합성 분석 및 실제 구조 문서화
- **분석 도구**: EntitySchemaAnalyzer v1.0

---

## 🔍 분석 개요

### 분석 범위
- **총 엔티티 수**: 55개
- **주요 도메인**: 
  - User (사용자 관리)
  - Chat (실시간 채팅)
  - Onboarding (온보딩)
  - Matching (매칭)
  - Session (세션)
  - Achievement (업적)
  - Notification (알림)
  - Level Test (레벨 테스트)

### 기술 스택
- **ORM**: Spring Data JPA + Hibernate
- **데이터베이스**: MySQL 8.0
- **캐시**: Redis 7
- **ID 생성 전략**: UUID (User), IDENTITY (대부분의 엔티티)

---

## 🏗️ 주요 엔티티 구조

### 1. User 도메인

#### User 엔티티
```java
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "USER_ID")
    private UUID userId;
    
    @Column(name = "EMAIL", unique = true)
    private String email;
    
    @Column(name = "NAME")
    private String name;
    
    @Column(name = "ENGLISH_NAME")
    private String englishName;
    
    @Column(name = "PROFILE_IMAGE")
    private String profileImage;
    
    @Column(name = "SELF_BIO", length = 1000)
    private String selfBio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NATIVE_LANG_ID")
    private Language nativeLanguage;
}
```

**특징**:
- UUID를 기본키로 사용 (보안 강화)
- 네이버/구글 OAuth 프로필 정보 통합
- 온보딩 완료 상태 추적
- 사용자 상태 (UserStatus)와 1:1 관계

#### Location 엔티티
```java
@Entity
@Table(name = "LOCATIONS")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOCATION_ID")
    private int locationId;
    
    @Column(name = "COUNTRY")
    private String country;
    
    @Column(name = "CITY")
    private String city;
    
    @Column(name = "TIME_ZONE")
    private String timeZone;
}
```

---

### 2. Chat 도메인

#### ChatRoom 엔티티
```java
@Entity
@Table(name = "CHAT_ROOM")
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;
    
    @Column(name = "room_name", nullable = false)
    private String roomName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;  // ONE_TO_ONE, GROUP
    
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
}
```

**특징**:
- 1:1 채팅과 그룹 채팅 지원
- 최대 참여자 수 제한 (1:1은 2명, 그룹은 4명)
- 공개/비공개 채팅방 구분

#### ChatMessage 엔티티
```java
@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;  // TEXT, IMAGE, FILE, SYSTEM
}
```

---

### 3. Onboarding 도메인

#### 복합키 구조
온보딩 도메인은 사용자별 설정을 추적하기 위해 복합키를 사용:

```java
@Embeddable
public class OnboardLangLevelId implements Serializable {
    @Column(name = "LANG_ID")
    private int languageId;
    
    @Column(name = "USER_ID")
    private UUID userId;
}
```

#### 주요 온보딩 엔티티
- **OnboardLangLevel**: 언어별 현재/목표 레벨
- **OnboardMotivation**: 학습 동기
- **OnboardLearningStyle**: 학습 스타일
- **OnboardSchedule**: 학습 일정
- **OnboardTopic**: 관심 주제
- **OnboardPartner**: 파트너 선호도

---

### 4. Matching 도메인

#### MatchingQueue 엔티티
```java
@Entity
@Table(name = "MATCHING_QUEUE")
public class MatchingQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchingStatus status;  // WAITING, MATCHED, EXPIRED
    
    @Column(name = "priority_score")
    private Integer priorityScore;
}
```

---

## ⚠️ 스키마 불일치 사항

### 🔴 Critical Issues

#### 1. Location 엔티티 구조 완전 불일치
- **실제 구조**: `locationId(int)`, `country`, `city`, `timeZone`
- **문서 기록**: `id(bigint)`, `name`, `code`
- **영향**: 완전한 스키마 재설계 필요

#### 2. ID 전략 불일치
- **User**: UUID 사용 (문서에는 varchar(36)로 기록)
- **ChatRoom, Session, LevelTest**: IDENTITY 전략 (문서에는 UUID로 기록)
- **영향**: ID 타입 및 생성 전략 문서 업데이트 필요

### 🟡 Medium Issues

#### 1. 테이블 네이밍 컨벤션 불일치
- **실제**: UPPER_CASE (예: USERS, CHAT_ROOM)
- **문서**: snake_case (예: users, chat_room)
- **영향**: 네이밍 컨벤션 통일 필요

#### 2. 필드 존재 여부 불일치
- **User.email**: 실제로 존재 (문서에는 누락으로 기록)
- **영향**: PRD 정보 수정 필요

---

## 📈 정합성 통계

### 현재 상태
- **총 엔티티 수**: 55개
- **불일치 엔티티**: 6개
- **정합성 비율**: 89.1%
- **목표 정합성**: 95%
- **목표 달성 여부**: ❌

### 심각도별 분류
- **🔴 Critical**: 2건
- **🟡 High**: 4건
- **🟢 Medium**: 1건

---

## 🔧 개선 권장사항

### 1. 즉시 수정 필요
- [ ] Location 엔티티 스키마 문서 완전 재작성
- [ ] ID 생성 전략 문서 업데이트
- [ ] 테이블 네이밍 컨벤션 통일

### 2. 단계적 개선
- [ ] 모든 엔티티에 대한 상세 문서 작성
- [ ] ERD 다이어그램 업데이트
- [ ] Bean Validation 제약조건 문서화

### 3. 자동화 도구 활용
- [ ] EntitySchemaAnalyzer 정기 실행 (CI/CD 통합)
- [ ] 스키마 변경시 자동 문서 업데이트 프로세스 구축
- [ ] 불일치 감지시 알림 설정

---

## 📝 참고사항

### EntitySchemaAnalyzer 활용
```bash
# API 엔드포인트
GET /api/entity-analysis/metadata

# 응답 형식
{
  "analysisTimestamp": "2025-09-10T12:00:00",
  "jpaEntities": [...],
  "schemaInconsistencies": [...],
  "statistics": {
    "schemaConsistencyPercentage": 89.1,
    "targetConsistencyPercentage": 95.0
  }
}
```

### 모니터링
- Spring Boot Actuator 헬스체크 통합
- 실시간 스키마 정합성 모니터링 가능

---

*이 문서는 EntitySchemaAnalyzer v1.0을 통해 자동 생성된 분석 결과를 기반으로 작성되었습니다.*