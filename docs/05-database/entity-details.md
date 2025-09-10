# 📚 엔티티 상세 명세서

## 📅 문서 정보
- **버전**: 1.0
- **최종 업데이트**: 2025-09-10
- **작성자**: Backend Development Team
- **목적**: STUDYMATE 엔티티별 상세 구조 및 비즈니스 로직 문서화

---

## 🏛️ 공통 엔티티

### BaseTimeEntity
모든 엔티티가 상속받는 기본 시간 추적 엔티티

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**특징**:
- Spring Data JPA Auditing 활용
- 생성/수정 시간 자동 관리
- 모든 도메인 엔티티에서 상속

---

## 👤 User 도메인 엔티티

### User 엔티티

#### 주요 필드 설명
| 필드명 | 비즈니스 용도 | 제약사항 |
|--------|--------------|----------|
| userId | 사용자 고유 식별자 | UUID 자동 생성 |
| userIdentity | OAuth 제공자별 고유 ID | OAuth 인증시 필수 |
| email | 이메일 주소 | Unique, 알림 발송용 |
| englishName | 영어 이름 | 글로벌 매칭용 |
| selfBio | 자기소개 | 최대 1000자 |
| isOnboardingCompleted | 온보딩 완료 여부 | 매칭 가능 조건 |

#### 비즈니스 메서드
```java
// 네이버 프로필 업데이트
public void updateNaverProfile(String name, String birthday, 
                               String birthyear, String profileImage)

// 구글 프로필 업데이트  
public void updateGoogleProfile(String name, String profileImage)

// 온보딩 완료 처리
public void setIsOnboardingCompleted(Boolean isOnboardingCompleted)
```

#### 연관관계
- **Location**: ManyToOne (사용자의 위치)
- **Language**: ManyToOne (모국어)
- **UserStatus**: OneToOne (온라인 상태)
- **온보딩 엔티티들**: OneToMany (설정 정보)

### UserStatus 엔티티

#### 용도
사용자의 실시간 상태 관리

#### 주요 필드
| 필드명 | 설명 | 업데이트 주기 |
|--------|------|--------------|
| onlineStatus | 온라인 상태 (ONLINE/OFFLINE/AWAY) | 실시간 |
| lastActiveAt | 마지막 활동 시간 | 5분마다 |
| statusMessage | 상태 메시지 | 사용자 설정시 |

---

## 💬 Chat 도메인 엔티티

### ChatRoom 엔티티

#### 비즈니스 규칙
- **1:1 채팅방**: 최대 2명 참여
- **그룹 채팅방**: 최대 4명 참여
- **공개 채팅방**: 누구나 참여 가능
- **비공개 채팅방**: 초대 필요

#### 핵심 메서드
```java
// 참여자 추가
public void addParticipant(User user) {
    // 1. null 체크
    // 2. 채팅방 영속화 체크
    // 3. 중복 참여 체크
    // 4. 최대 인원 체크
    // 5. 참여자 추가
}

// 참여 가능 여부 확인
public boolean canJoin(UUID userId) {
    // 1. 이미 참여 중인지 확인
    // 2. 공개/비공개 확인
    // 3. 최대 인원 확인
}
```

### ChatMessage 엔티티

#### 메시지 타입
| 타입 | 용도 | 저장 형식 |
|------|------|----------|
| TEXT | 일반 텍스트 | content 필드에 직접 저장 |
| IMAGE | 이미지 메시지 | URL 저장 + ChatImage 연결 |
| FILE | 파일 첨부 | URL 저장 + ChatFile 연결 |
| SYSTEM | 시스템 메시지 | 입장/퇴장 등 자동 생성 |

#### 읽음 처리
- MessageReadStatus 테이블로 개별 관리
- 읽음 시간 기록
- 안 읽은 메시지 카운트 계산 가능

### ChatRoomParticipant 엔티티

#### 복합키 구조
```java
@Embeddable
public class ChatRoomParticipantId implements Serializable {
    private Long roomId;
    private UUID userId;
}
```

#### 용도
- 채팅방-사용자 다대다 관계 관리
- 참여 시간 추적
- 마지막 읽은 시간 관리

---

## 📝 Onboarding 도메인 엔티티

### 설계 철학
- 복합키를 통한 사용자별 설정 관리
- 다중 선택 가능한 항목들 지원
- 유연한 확장 가능 구조

### Language 엔티티

#### 지원 언어
| 언어 | 코드 | 우선순위 |
|------|------|----------|
| 한국어 | ko | 1 |
| 영어 | en | 2 |
| 일본어 | ja | 3 |
| 중국어 | zh | 4 |
| 스페인어 | es | 5 |

### OnboardLangLevel 엔티티

#### 복합키 구조
- userId + languageId 조합
- 사용자별 여러 언어 레벨 관리 가능

#### 레벨 체계
| 레벨 | CEFR | 설명 |
|------|------|------|
| Beginner | A1-A2 | 기초 회화 가능 |
| Intermediate | B1-B2 | 일상 대화 가능 |
| Advanced | C1-C2 | 유창한 대화 가능 |

### OnboardMotivation 엔티티

#### 학습 동기 유형
- Career (경력 개발)
- Travel (여행)
- Culture (문화 교류)
- Academic (학업)
- Personal (개인적 관심)

#### 우선순위 관리
- priority 필드로 중요도 설정
- 매칭 알고리즘에 활용

### OnboardSchedule 엔티티

#### 스케줄 관리
```java
// 요일별 가능 시간대
MONDAY_MORNING (월요일 오전)
MONDAY_AFTERNOON (월요일 오후)
MONDAY_EVENING (월요일 저녁)
// ... 각 요일별 반복
```

---

## 🤝 Matching 도메인 엔티티

### MatchingQueue 엔티티

#### 상태 관리
| 상태 | 설명 | 다음 상태 |
|------|------|----------|
| WAITING | 매칭 대기중 | MATCHED, EXPIRED |
| MATCHED | 매칭 완료 | - |
| EXPIRED | 시간 초과 | WAITING (재시도) |

#### 우선순위 점수 계산
```java
// 우선순위 요소
- 대기 시간 (오래 기다릴수록 높음)
- 프로필 완성도
- 활동 점수
- 프리미엄 여부
```

### UserMatch 엔티티

#### 매칭 점수 알고리즘
```java
// 매칭 점수 계산 요소 (100점 만점)
- 언어 레벨 호환성 (30점)
- 학습 목표 일치도 (25점)
- 시간대 겹침 (20점)
- 관심사 공통점 (15점)
- 위치 근접성 (10점)
```

#### 매칭 종료 사유
- USER_REQUEST (사용자 요청)
- INACTIVITY (비활성)
- VIOLATION (규정 위반)
- SYSTEM (시스템 결정)

---

## 📚 Session 도메인 엔티티

### Session 엔티티

#### 세션 생명주기
```
SCHEDULED → IN_PROGRESS → COMPLETED
         ↘              ↗
            CANCELLED
```

#### 세션 타입별 규칙
| 타입 | 최소 인원 | 최대 인원 | 기본 시간 |
|------|----------|----------|----------|
| ONE_TO_ONE | 2 | 2 | 30분 |
| GROUP | 3 | 6 | 60분 |
| WORKSHOP | 5 | 20 | 90분 |

### SessionBooking 엔티티

#### 예약 상태
- PENDING (대기중)
- CONFIRMED (확정)
- WAITLISTED (대기자)
- CANCELLED (취소)

#### 비즈니스 규칙
- 세션 시작 24시간 전까지 취소 가능
- 노쇼 3회시 패널티
- 대기자는 자동 승격

---

## 🏆 Achievement 도메인 엔티티

### Achievement 엔티티

#### 업적 카테고리
| 카테고리 | 설명 | 예시 |
|----------|------|------|
| LEARNING | 학습 관련 | 연속 7일 학습 |
| SOCIAL | 소셜 활동 | 친구 10명 만들기 |
| MILESTONE | 마일스톤 | 100시간 달성 |
| SPECIAL | 특별 이벤트 | 창립 기념일 참여 |

### UserAchievement 엔티티

#### 진행률 관리
```java
// 진행형 업적
progress: 0-100 (%)
// 완료시 earned_at 기록

// 반복 가능 업적
tier: BRONZE, SILVER, GOLD, PLATINUM
```

---

## 🔔 Notification 도메인 엔티티

### Notification 엔티티

#### 알림 타입
| 타입 | 트리거 | 우선순위 |
|------|--------|----------|
| MATCH_FOUND | 매칭 성공 | HIGH |
| MESSAGE_RECEIVED | 새 메시지 | MEDIUM |
| SESSION_REMINDER | 세션 리마인더 | HIGH |
| ACHIEVEMENT_EARNED | 업적 달성 | LOW |
| SYSTEM_NOTICE | 시스템 공지 | MEDIUM |

### NotificationPreference 엔티티

#### 알림 채널 관리
```java
// 채널별 활성화
push_enabled: 모바일 푸시
email_enabled: 이메일
sms_enabled: SMS

// 방해금지 시간
quiet_hours_start: 22:00
quiet_hours_end: 08:00
```

---

## 🔐 보안 고려사항

### 데이터 암호화
- 비밀번호: BCrypt 해싱
- 민감 정보: AES-256 암호화
- 통신: TLS 1.3

### 접근 제어
- UUID 사용으로 ID 추측 방지
- 외래키 제약으로 무결성 보장
- Soft Delete로 데이터 보존

---

## 🚀 성능 최적화

### Lazy Loading 전략
- 모든 연관관계 기본 LAZY
- 필요시 Fetch Join 사용
- N+1 문제 방지

### 캐싱 전략
- 2차 캐시: 자주 조회되는 마스터 데이터
- 쿼리 캐시: 복잡한 통계 쿼리
- Redis 캐시: 세션, 실시간 데이터

---

## 📈 모니터링 포인트

### 주요 메트릭
- 테이블별 레코드 수
- 쿼리 실행 시간
- 인덱스 사용률
- 캐시 히트율

### 알람 설정
- Slow Query: 1초 이상
- 데드락 감지
- 연결 풀 고갈
- 디스크 사용률 80% 초과

---

*이 문서는 Spring JPA 엔티티 코드를 기반으로 작성되었습니다.*
*각 엔티티의 실제 구현은 소스 코드를 참조하세요.*