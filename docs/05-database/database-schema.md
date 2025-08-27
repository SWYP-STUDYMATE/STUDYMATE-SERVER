# 🗄️ 데이터베이스 스키마 설계

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Backend Development Team
- **목적**: STUDYMATE 데이터베이스 스키마 및 ERD

---

## 🏗️ 데이터베이스 구조 개요

### 사용 기술
- **주 데이터베이스**: MySQL 8.0 (Docker 컨테이너)
- **캐시**: Redis 7 (Docker 컨테이너)
- **ORM**: Spring Data JPA + Hibernate
- **마이그레이션**: Flyway (계획)

### 설계 원칙
1. **정규화**: 3NF까지 정규화하여 데이터 중복 최소화
2. **확장성**: 향후 기능 확장을 고려한 유연한 구조
3. **성능**: 자주 조회되는 컬럼에 인덱스 설정
4. **일관성**: 일관된 네이밍 컨벤션 적용

---

## 📊 ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    USERS {
        varchar(36) id PK "UUID"
        varchar(255) email UK
        varchar(255) name
        varchar(255) english_name
        varchar(255) profile_image_url
        enum gender_type
        text self_bio
        int birth_year
        varchar(10) birth_day
        bigint location_id FK
        datetime created_at
        datetime updated_at
    }
    
    LOCATIONS {
        bigint id PK
        varchar(255) name
        varchar(50) code
        datetime created_at
        datetime updated_at
    }
    
    ONBOARD_LANGUAGES {
        varchar(36) user_id PK,FK
        bigint language_id PK,FK
        bigint level_id FK
        datetime created_at
    }
    
    LANGUAGES {
        bigint id PK
        varchar(255) name
        varchar(10) code
        boolean is_active
    }
    
    LANGUAGE_LEVELS {
        bigint id PK
        varchar(50) level_name
        varchar(10) level_code
        int level_order
    }
    
    ONBOARD_INTERESTS {
        varchar(36) user_id PK,FK
        bigint motivation_id PK,FK
        datetime created_at
    }
    
    MOTIVATIONS {
        bigint id PK
        varchar(255) name
        varchar(255) description
        boolean is_active
    }
    
    ONBOARD_PARTNERS {
        varchar(36) user_id PK,FK
        bigint partner_preference_id PK,FK
        datetime created_at
    }
    
    PARTNER_PREFERENCES {
        bigint id PK
        enum preference_type
        varchar(255) preference_value
        varchar(255) display_name
    }
    
    ONBOARD_SCHEDULES {
        varchar(36) user_id PK,FK
        enum day_of_week PK
        varchar(20) time_slot PK
        datetime created_at
    }
    
    LEVEL_TESTS {
        varchar(36) id PK "UUID"
        varchar(36) user_id FK
        enum test_status
        json questions
        json responses
        json analysis_result
        varchar(10) overall_level
        int pronunciation_score
        int fluency_score
        int vocabulary_score
        int grammar_score
        text feedback
        datetime started_at
        datetime completed_at
        datetime created_at
    }
    
    CHAT_ROOMS {
        varchar(36) id PK "UUID"
        varchar(255) room_name
        enum room_type
        json metadata
        datetime created_at
        datetime updated_at
    }
    
    CHAT_PARTICIPANTS {
        varchar(36) room_id PK,FK
        varchar(36) user_id PK,FK
        enum participant_role
        datetime joined_at
        datetime last_read_at
    }
    
    CHAT_MESSAGES {
        varchar(36) id PK "UUID"
        varchar(36) room_id FK
        varchar(36) sender_id FK
        enum message_type
        text content
        varchar(255) file_url
        json metadata
        boolean is_deleted
        datetime created_at
        datetime updated_at
    }
    
    SESSIONS {
        varchar(36) id PK "UUID"
        varchar(36) host_id FK
        enum session_type
        varchar(255) title
        text description
        datetime scheduled_time
        int duration_minutes
        enum session_status
        json webrtc_config
        datetime created_at
        datetime updated_at
    }
    
    SESSION_PARTICIPANTS {
        varchar(36) session_id PK,FK
        varchar(36) user_id PK,FK
        enum participant_status
        datetime joined_at
        datetime left_at
    }

    %% Relationships
    USERS ||--|| LOCATIONS : belongs_to
    USERS ||--o{ ONBOARD_LANGUAGES : has_many
    USERS ||--o{ ONBOARD_INTERESTS : has_many
    USERS ||--o{ ONBOARD_PARTNERS : has_many
    USERS ||--o{ ONBOARD_SCHEDULES : has_many
    USERS ||--o{ LEVEL_TESTS : has_many
    USERS ||--o{ CHAT_PARTICIPANTS : has_many
    USERS ||--o{ CHAT_MESSAGES : sends
    USERS ||--o{ SESSIONS : hosts
    USERS ||--o{ SESSION_PARTICIPANTS : participates
    
    LANGUAGES ||--o{ ONBOARD_LANGUAGES : has_many
    LANGUAGE_LEVELS ||--o{ ONBOARD_LANGUAGES : has_many
    MOTIVATIONS ||--o{ ONBOARD_INTERESTS : has_many
    PARTNER_PREFERENCES ||--o{ ONBOARD_PARTNERS : has_many
    
    CHAT_ROOMS ||--o{ CHAT_PARTICIPANTS : has_many
    CHAT_ROOMS ||--o{ CHAT_MESSAGES : contains
    
    SESSIONS ||--o{ SESSION_PARTICIPANTS : has_many
```

---

## 📋 테이블 상세 스키마

### 1. 사용자 관리 테이블

#### users (사용자 기본 정보)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | VARCHAR(36) | PK | UUID 형태의 사용자 식별자 |
| `email` | VARCHAR(255) | UK, NOT NULL | 이메일 (소셜 로그인용) |
| `name` | VARCHAR(255) | NOT NULL | 사용자 이름 |
| `english_name` | VARCHAR(255) | NULL | 영어 이름 |
| `profile_image_url` | VARCHAR(255) | NULL | 프로필 이미지 URL |
| `gender_type` | ENUM | NULL | MALE, FEMALE, OTHER |
| `self_bio` | TEXT | NULL | 자기소개 |
| `birth_year` | INT | NULL | 출생연도 |
| `birth_day` | VARCHAR(10) | NULL | 생일 (MM-DD) |
| `location_id` | BIGINT | FK | 위치 정보 참조 |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |
| `updated_at` | DATETIME | ON UPDATE NOW() | 수정일시 |

**인덱스:**
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_location ON users(location_id);
```

#### locations (위치 정보)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 위치 식별자 |
| `name` | VARCHAR(255) | NOT NULL | 위치명 (서울특별시) |
| `code` | VARCHAR(50) | UK, NOT NULL | 위치 코드 (SEOUL) |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |
| `updated_at` | DATETIME | ON UPDATE NOW() | 수정일시 |

### 2. 온보딩 관련 테이블

#### onboard_languages (언어 설정)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `user_id` | VARCHAR(36) | PK, FK | 사용자 ID |
| `language_id` | BIGINT | PK, FK | 언어 ID |
| `level_id` | BIGINT | FK | 언어 레벨 ID |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |

**복합키:** `(user_id, language_id)`

#### languages (언어 마스터)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 언어 식별자 |
| `name` | VARCHAR(255) | NOT NULL | 언어명 (English) |
| `code` | VARCHAR(10) | UK, NOT NULL | 언어 코드 (EN) |
| `is_active` | BOOLEAN | DEFAULT TRUE | 활성화 여부 |

**초기 데이터:**
```sql
INSERT INTO languages (name, code) VALUES 
('English', 'EN'),
('Korean', 'KO'),
('Japanese', 'JA'),
('Chinese', 'ZH');
```

#### language_levels (언어 레벨)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 레벨 식별자 |
| `level_name` | VARCHAR(50) | NOT NULL | 레벨명 (Beginner) |
| `level_code` | VARCHAR(10) | UK, NOT NULL | CEFR 코드 (A1) |
| `level_order` | INT | NOT NULL | 정렬 순서 |

**초기 데이터:**
```sql
INSERT INTO language_levels (level_name, level_code, level_order) VALUES 
('Beginner', 'A1', 1),
('Elementary', 'A2', 2),
('Intermediate', 'B1', 3),
('Upper Intermediate', 'B2', 4),
('Advanced', 'C1', 5),
('Proficient', 'C2', 6);
```

### 3. 채팅 관련 테이블

#### chat_rooms (채팅방)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | VARCHAR(36) | PK | UUID 형태의 채팅방 ID |
| `room_name` | VARCHAR(255) | NOT NULL | 채팅방 이름 |
| `room_type` | ENUM | NOT NULL | DIRECT, GROUP |
| `metadata` | JSON | NULL | 추가 메타데이터 |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |
| `updated_at` | DATETIME | ON UPDATE NOW() | 수정일시 |

#### chat_messages (채팅 메시지)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | VARCHAR(36) | PK | UUID 형태의 메시지 ID |
| `room_id` | VARCHAR(36) | FK, NOT NULL | 채팅방 ID |
| `sender_id` | VARCHAR(36) | FK, NOT NULL | 발신자 ID |
| `message_type` | ENUM | NOT NULL | TEXT, IMAGE, VOICE, FILE |
| `content` | TEXT | NULL | 메시지 내용 |
| `file_url` | VARCHAR(255) | NULL | 첨부파일 URL |
| `metadata` | JSON | NULL | 추가 메타데이터 |
| `is_deleted` | BOOLEAN | DEFAULT FALSE | 삭제 여부 |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |
| `updated_at` | DATETIME | ON UPDATE NOW() | 수정일시 |

**인덱스:**
```sql
CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at DESC);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
```

### 4. 세션 관리 테이블

#### sessions (화상/음성 세션)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | VARCHAR(36) | PK | UUID 형태의 세션 ID |
| `host_id` | VARCHAR(36) | FK, NOT NULL | 호스트 사용자 ID |
| `session_type` | ENUM | NOT NULL | VIDEO, AUDIO |
| `title` | VARCHAR(255) | NOT NULL | 세션 제목 |
| `description` | TEXT | NULL | 세션 설명 |
| `scheduled_time` | DATETIME | NOT NULL | 예정 시간 |
| `duration_minutes` | INT | DEFAULT 60 | 예상 소요시간 |
| `session_status` | ENUM | DEFAULT 'SCHEDULED' | SCHEDULED, ACTIVE, COMPLETED, CANCELLED |
| `webrtc_config` | JSON | NULL | WebRTC 설정 |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |
| `updated_at` | DATETIME | ON UPDATE NOW() | 수정일시 |

### 5. 레벨 테스트 테이블

#### level_tests (AI 레벨 테스트)
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| `id` | VARCHAR(36) | PK | UUID 형태의 테스트 ID |
| `user_id` | VARCHAR(36) | FK, NOT NULL | 사용자 ID |
| `test_status` | ENUM | DEFAULT 'STARTED' | STARTED, COMPLETED, FAILED |
| `questions` | JSON | NULL | 문제 목록 |
| `responses` | JSON | NULL | 사용자 답변 |
| `analysis_result` | JSON | NULL | AI 분석 결과 |
| `overall_level` | VARCHAR(10) | NULL | 전체 레벨 (B2) |
| `pronunciation_score` | INT | NULL | 발음 점수 (0-100) |
| `fluency_score` | INT | NULL | 유창성 점수 |
| `vocabulary_score` | INT | NULL | 어휘 점수 |
| `grammar_score` | INT | NULL | 문법 점수 |
| `feedback` | TEXT | NULL | 피드백 내용 |
| `started_at` | DATETIME | NULL | 테스트 시작 시간 |
| `completed_at` | DATETIME | NULL | 테스트 완료 시간 |
| `created_at` | DATETIME | DEFAULT NOW() | 생성일시 |

**인덱스:**
```sql
CREATE INDEX idx_level_tests_user_created ON level_tests(user_id, created_at DESC);
```

---

## 🔧 데이터베이스 설정

### MySQL 설정 (my.cnf)
```ini
[mysqld]
# Character Set
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# Performance
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
max_connections = 200

# Logging
slow_query_log = 1
long_query_time = 2

# Binary Log
log-bin = mysql-bin
expire_logs_days = 7
```

### Redis 설정
```bash
# Memory Configuration
maxmemory 512mb
maxmemory-policy allkeys-lru

# Persistence
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec
```

---

## 📊 인덱스 전략

### 주요 쿼리 패턴별 인덱스

#### 사용자 조회 최적화
```sql
-- 이메일로 사용자 찾기 (로그인)
CREATE INDEX idx_users_email ON users(email);

-- 위치별 사용자 검색
CREATE INDEX idx_users_location ON users(location_id);
```

#### 채팅 성능 최적화
```sql
-- 채팅방별 최근 메시지 조회
CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at DESC);

-- 사용자별 채팅 참여 조회
CREATE INDEX idx_chat_participants_user ON chat_participants(user_id);
```

#### 세션 조회 최적화
```sql
-- 사용자별 세션 목록
CREATE INDEX idx_sessions_host_scheduled ON sessions(host_id, scheduled_time);

-- 날짜별 세션 조회
CREATE INDEX idx_sessions_scheduled_time ON sessions(scheduled_time);
```

### 복합 인덱스 전략
```sql
-- 복합 인덱스: 자주 함께 조회되는 컬럼들
CREATE INDEX idx_chat_messages_room_type_created 
ON chat_messages(room_id, message_type, created_at DESC);

-- 온보딩 관련 복합 인덱스
CREATE INDEX idx_onboard_languages_user_lang 
ON onboard_languages(user_id, language_id);
```

---

## 🔄 데이터 마이그레이션

### Flyway 마이그레이션 스크립트 예시

#### V1__Create_base_tables.sql
```sql
-- 기본 테이블 생성
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### V2__Add_user_profile_columns.sql
```sql
-- 사용자 프로필 컬럼 추가
ALTER TABLE users 
ADD COLUMN english_name VARCHAR(255),
ADD COLUMN profile_image_url VARCHAR(255),
ADD COLUMN gender_type ENUM('MALE', 'FEMALE', 'OTHER'),
ADD COLUMN self_bio TEXT,
ADD COLUMN birth_year INT,
ADD COLUMN birth_day VARCHAR(10),
ADD COLUMN location_id BIGINT;

-- 외래키 제약조건 추가
ALTER TABLE users 
ADD FOREIGN KEY (location_id) REFERENCES locations(id);
```

### 초기 마스터 데이터
```sql
-- 기본 위치 데이터
INSERT INTO locations (name, code) VALUES 
('서울특별시', 'SEOUL'),
('부산광역시', 'BUSAN'),
('대구광역시', 'DAEGU'),
('인천광역시', 'INCHEON');

-- 기본 언어 데이터  
INSERT INTO languages (name, code) VALUES 
('English', 'EN'),
('Korean', 'KO'),
('Japanese', 'JA'),
('Chinese', 'ZH'),
('Spanish', 'ES'),
('French', 'FR');

-- CEFR 레벨 데이터
INSERT INTO language_levels (level_name, level_code, level_order) VALUES 
('Beginner', 'A1', 1),
('Elementary', 'A2', 2),
('Intermediate', 'B1', 3),
('Upper Intermediate', 'B2', 4),
('Advanced', 'C1', 5),
('Proficient', 'C2', 6);
```

---

## 🔍 쿼리 최적화

### 자주 사용되는 쿼리 패턴

#### 사용자 프로필 조회 (N+1 문제 해결)
```java
// JPA Repository에서 fetch join 사용
@Query("SELECT u FROM User u " +
       "LEFT JOIN FETCH u.location " +
       "LEFT JOIN FETCH u.onboardLanguages ol " +
       "LEFT JOIN FETCH ol.language " +
       "LEFT JOIN FETCH ol.level " +
       "WHERE u.id = :userId")
Optional<User> findByIdWithDetails(@Param("userId") String userId);
```

#### 채팅 메시지 페이징
```java
// 페이징과 정렬을 함께 사용
@Query("SELECT cm FROM ChatMessage cm " +
       "WHERE cm.roomId = :roomId " +
       "AND cm.isDeleted = false " +
       "ORDER BY cm.createdAt DESC")
Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(
    @Param("roomId") String roomId, 
    Pageable pageable
);
```

### 성능 모니터링 쿼리
```sql
-- 느린 쿼리 확인
SELECT * FROM mysql.slow_log 
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY query_time DESC;

-- 인덱스 사용률 확인
SHOW INDEX FROM users;
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';
```

---

## 🛡️ 데이터 보안

### 민감 정보 보호
```java
// 개인정보 암호화 (예시)
@Convert(converter = EncryptionConverter.class)
private String email;

@Convert(converter = EncryptionConverter.class) 
private String selfBio;
```

### 접근 제어
```sql
-- 애플리케이션 전용 사용자 생성
CREATE USER 'studymate_app'@'%' IDENTIFIED BY 'secure_password';

-- 필요한 권한만 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON studymate.* TO 'studymate_app'@'%';

-- 민감한 시스템 테이블 접근 제한
REVOKE ALL ON mysql.* FROM 'studymate_app'@'%';
```

---

## 📈 확장성 고려사항

### 수직적 확장 (Scale Up)
- **CPU/Memory 증설**: 복잡한 조인 쿼리 성능 향상
- **SSD 사용**: I/O 성능 개선
- **Connection Pool 최적화**: HikariCP 설정 튜닝

### 수평적 확장 (Scale Out)
- **읽기 전용 복제본**: Master-Slave 구성
- **샤딩**: 사용자 ID 기반 데이터 분산
- **캐시 계층**: Redis Cluster 도입

### 미래 고려사항
```sql
-- 대용량 데이터 처리를 위한 파티셔닝
ALTER TABLE chat_messages 
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028)
);
```

---

## 📚 관련 문서

- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [API 레퍼런스](../04-api/api-reference.md)
- [백엔드 서비스](../07-backend/services-overview.md)
- [배포 가이드](../08-infrastructure/deployment-guide.md)