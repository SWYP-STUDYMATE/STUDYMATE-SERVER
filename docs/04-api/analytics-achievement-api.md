# 📊 Analytics & Achievement API 레퍼런스

## 📅 문서 정보
- **최종 업데이트**: 2025-08-30
- **작성자**: Backend Development Team
- **목적**: Analytics 및 Achievement 시스템 API 문서화
- **베이스 URL**: `https://api.languagemate.kr`

---

## 📊 Analytics API

### 내 학습 통계 조회
현재 사용자의 학습 통계 및 진도를 조회합니다.

**GET** `/api/v1/analytics/users/my-stats`

#### 인증
- JWT 토큰 필요

#### 응답
```json
{
  "success": true,
  "data": {
    "totalXp": 2450,
    "currentStreak": 7,
    "totalStudyTimeMinutes": 1680,
    "totalSessions": 15,
    "totalMessages": 234,
    "totalWordsLearned": 156,
    "totalTestsTaken": 3,
    "averageTestScore": 85.6,
    "totalBadgesEarned": 8,
    "languageProgress": {
      "english": 2100,
      "japanese": 350
    },
    "skillProgress": {
      "SPEAKING": 420,
      "LISTENING": 380,
      "READING": 310,
      "WRITING": 570
    },
    "dailyProgress": [
      {
        "date": "2025-08-29",
        "xpEarned": 120,
        "studyMinutes": 60,
        "messagesSent": 15,
        "wasActive": true
      }
    ],
    "achievements": [
      {
        "title": "Week Warrior",
        "description": "7-day learning streak",
        "earnedDate": "2025-08-29",
        "iconUrl": "/badges/streak-7.png"
      }
    ]
  },
  "message": "학습 통계 조회가 완료되었습니다."
}
```

### 기간별 학습 통계 조회
특정 기간 동안의 학습 통계를 조회합니다.

**GET** `/api/v1/analytics/users/my-stats/range`

#### 쿼리 파라미터
- `startDate` (required): 시작 날짜 (YYYY-MM-DD)
- `endDate` (required): 종료 날짜 (YYYY-MM-DD)

#### 예시 요청
```
GET /api/v1/analytics/users/my-stats/range?startDate=2025-08-01&endDate=2025-08-30
```

### 특정 사용자 통계 조회 (관리자 전용)
특정 사용자의 학습 통계를 조회합니다.

**GET** `/api/v1/analytics/users/{userId}/stats`

#### 경로 파라미터
- `userId` (required): 사용자 UUID

#### 인증
- JWT 토큰 필요
- ADMIN 권한 필요

### 시스템 전체 분석 조회 (관리자 전용)
시스템 전체의 분석 통계를 조회합니다.

**GET** `/api/v1/analytics/system`

#### 인증
- JWT 토큰 필요
- ADMIN 권한 필요

#### 응답
```json
{
  "success": true,
  "data": {
    "totalUsers": 5420,
    "activeUsersToday": 234,
    "activeUsersThisWeek": 1456,
    "activeUsersThisMonth": 3210,
    "totalSessions": 12450,
    "completedSessionsToday": 156,
    "averageSessionDuration": 45.6,
    "totalMessages": 89234,
    "messagesThisWeek": 5678,
    "usersByLanguage": {
      "english": 3200,
      "korean": 1500,
      "japanese": 720
    },
    "userGrowthTrend": [
      {
        "date": "2025-08-29T00:00:00",
        "value": 5420,
        "metricName": "USER_GROWTH"
      }
    ],
    "systemHealth": {
      "successRate": 99.2,
      "averageResponseTime": 145.6,
      "errorCount": 12,
      "systemStatus": "HEALTHY"
    }
  },
  "message": "시스템 분석 데이터 조회가 완료되었습니다."
}
```

### 기간별 시스템 분석 (관리자 전용)
**GET** `/api/v1/analytics/system/range`

#### 쿼리 파라미터
- `startDate` (required): 시작 날짜시간 (ISO DateTime)
- `endDate` (required): 종료 날짜시간 (ISO DateTime)

### 사용자 활동 기록
사용자의 활동을 추적하여 분석 데이터로 저장합니다.

**POST** `/api/v1/analytics/activities/record`

#### 쿼리 파라미터
- `activityType` (required): 활동 타입
  - 가능한 값: LOGIN, LOGOUT, SESSION_JOIN, MESSAGE_SENT, PROFILE_UPDATE 등
- `activityCategory` (required): 활동 카테고리  
  - 가능한 값: AUTH, SESSION, CHAT, PROFILE 등
- `description` (optional): 활동 설명
- `metadata` (optional): 추가 메타데이터 (JSON 형식)

#### 헤더
- `X-Forwarded-For` (optional): IP 주소
- `User-Agent` (optional): 사용자 에이전트

#### 예시 요청
```
POST /api/v1/analytics/activities/record?activityType=SESSION_JOIN&activityCategory=SESSION&description=Started video session
Authorization: Bearer {token}
X-Forwarded-For: 192.168.1.100
User-Agent: Mozilla/5.0...
```

### 학습 진도 업데이트
**POST** `/api/v1/analytics/learning-progress/update`

#### 쿼리 파라미터
- `languageCode` (required): 언어 코드 (en, ko, ja 등)
- `progressType` (required): 진도 타입
  - 가능한 값: SESSION_COMPLETED, MESSAGE_SENT, WORDS_LEARNED, TEST_COMPLETED 등
- `value` (required): 진도 값 (숫자)
- `metadata` (optional): 추가 메타데이터

### 시스템 메트릭 기록 (관리자 전용)
**POST** `/api/v1/analytics/metrics/record`

#### 쿼리 파라미터
- `metricName` (required): 메트릭 이름
- `metricCategory` (required): 메트릭 카테고리
- `metricValue` (required): 메트릭 값
- `metricUnit` (optional): 측정 단위
- `aggregationPeriod` (optional): 집계 기간

### 메트릭 계산 (관리자 전용)
시스템 메트릭을 일별/주별/월별로 계산합니다.

**POST** `/api/v1/analytics/metrics/calculate-daily`
**POST** `/api/v1/analytics/metrics/calculate-weekly`
**POST** `/api/v1/analytics/metrics/calculate-monthly`

#### 쿼리 파라미터
- `date` (daily): 계산할 날짜 (YYYY-MM-DD)
- `weekStart` (weekly): 주 시작 날짜 (YYYY-MM-DD)
- `monthStart` (monthly): 월 시작 날짜 (YYYY-MM-DD)

---

## 🏆 Achievement API

### 모든 활성화된 성취 조회
모든 활성화된 성취 목록을 조회합니다.

**GET** `/api/v1/achievements`

#### 응답
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "key": "first_session",
      "title": "첫 세션 완료",
      "description": "첫 번째 언어 교환 세션을 완료하세요",
      "iconUrl": "/achievements/first_session.png",
      "category": "SESSION",
      "targetValue": 1,
      "rewardXp": 100,
      "rewardBadge": "first_session_badge",
      "isActive": true
    },
    {
      "id": 2,
      "key": "chat_master",
      "title": "채팅 마스터",
      "description": "100개의 메시지를 보내세요",
      "iconUrl": "/achievements/chat_master.png",
      "category": "CHAT",
      "targetValue": 100,
      "rewardXp": 250,
      "rewardBadge": "chat_master_badge",
      "isActive": true
    }
  ]
}
```

### 카테고리별 성취 조회
특정 카테고리의 성취 목록을 조회합니다.

**GET** `/api/v1/achievements/category/{category}`

#### 경로 파라미터
- `category` (required): 성취 카테고리
  - 가능한 값: SESSION, CHAT, LEARNING, SOCIAL, PROFILE, TEST 등

#### 예시 요청
```
GET /api/v1/achievements/category/SESSION
```

### 내 성취 현황 조회
사용자의 모든 성취 현황을 조회합니다.

**GET** `/api/v1/achievements/my`

#### 응답
```json
{
  "success": true,
  "data": [
    {
      "userAchievementId": 101,
      "achievement": {
        "id": 1,
        "key": "first_session",
        "title": "첫 세션 완료",
        "description": "첫 번째 언어 교환 세션을 완료하세요",
        "category": "SESSION",
        "targetValue": 1,
        "rewardXp": 100
      },
      "currentProgress": 1,
      "isCompleted": true,
      "completedAt": "2025-08-29T10:30:00Z",
      "isRewardClaimed": true,
      "claimedAt": "2025-08-29T10:35:00Z"
    },
    {
      "userAchievementId": 102,
      "achievement": {
        "id": 2,
        "key": "chat_master",
        "title": "채팅 마스터",
        "description": "100개의 메시지를 보내세요",
        "category": "CHAT",
        "targetValue": 100,
        "rewardXp": 250
      },
      "currentProgress": 45,
      "isCompleted": false,
      "completedAt": null,
      "isRewardClaimed": false,
      "claimedAt": null
    }
  ]
}
```

### 내 완료된 성취 조회
**GET** `/api/v1/achievements/my/completed`

### 내 진행 중인 성취 조회
**GET** `/api/v1/achievements/my/in-progress`

### 내 성취 통계 조회
사용자의 성취 통계 정보를 조회합니다.

**GET** `/api/v1/achievements/my/stats`

#### 응답
```json
{
  "success": true,
  "data": {
    "totalAchievements": 25,
    "completedAchievements": 12,
    "inProgressAchievements": 8,
    "lockedAchievements": 5,
    "totalXpEarned": 2400,
    "completionRate": 48.0,
    "categoryStats": {
      "SESSION": {
        "completed": 5,
        "total": 8,
        "completionRate": 62.5
      },
      "CHAT": {
        "completed": 3,
        "total": 6,
        "completionRate": 50.0
      },
      "LEARNING": {
        "completed": 4,
        "total": 7,
        "completionRate": 57.1
      }
    },
    "recentCompletions": [
      {
        "achievementTitle": "첫 세션 완료",
        "completedAt": "2025-08-29T10:30:00Z",
        "xpEarned": 100
      }
    ]
  }
}
```

### 성취 진행도 업데이트
특정 성취의 진행도를 업데이트합니다.

**POST** `/api/v1/achievements/progress`

#### 요청 바디
```json
{
  "achievementKey": "session_count",
  "progress": 5
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "userAchievementId": 102,
    "achievement": {
      "key": "session_count",
      "title": "세션 마스터"
    },
    "currentProgress": 5,
    "isCompleted": false,
    "progressPercentage": 50.0
  }
}
```

### 성취 진행도 증가
특정 성취의 진행도를 증가시킵니다.

**POST** `/api/v1/achievements/progress/increment`

#### 쿼리 파라미터
- `achievementKey` (required): 성취 키
- `increment` (optional): 증가량 (기본값: 1)

#### 예시 요청
```
POST /api/v1/achievements/progress/increment?achievementKey=message_count&increment=1
```

### 보상 수령
완료된 성취의 보상을 수령합니다.

**POST** `/api/v1/achievements/{userAchievementId}/claim-reward`

#### 경로 파라미터
- `userAchievementId` (required): 사용자 성취 ID

#### 응답
```json
{
  "success": true,
  "data": {
    "userAchievementId": 101,
    "isRewardClaimed": true,
    "claimedAt": "2025-08-30T14:22:00Z",
    "rewards": {
      "xp": 100,
      "badge": "first_session_badge",
      "title": "세션 초보자"
    }
  }
}
```

### 성취 초기화
사용자의 성취를 초기화합니다. (주로 새 사용자 등록시 사용)

**POST** `/api/v1/achievements/initialize`

#### 응답
```json
{
  "success": true,
  "data": "성취 초기화가 완료되었습니다."
}
```

### 성취 완료 확인
사용자의 성취 완료 여부를 확인하고 자동으로 완료 처리합니다.

**POST** `/api/v1/achievements/check-completion`

#### 응답
```json
{
  "success": true,
  "data": [
    {
      "userAchievementId": 103,
      "achievement": {
        "title": "채팅 초보자",
        "rewardXp": 50
      },
      "justCompleted": true,
      "completedAt": "2025-08-30T14:25:00Z"
    }
  ]
}
```

---

## 🚨 에러 코드

### Analytics 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| ANALYTICS_001 | 400 | 잘못된 날짜 형식 |
| ANALYTICS_002 | 400 | 잘못된 활동 타입 |
| ANALYTICS_003 | 403 | 관리자 권한 필요 |
| ANALYTICS_004 | 404 | 사용자 통계를 찾을 수 없음 |

### Achievement 관련  
| 코드 | HTTP | 설명 |
|------|------|------|
| ACHIEVEMENT_001 | 404 | 성취를 찾을 수 없음 |
| ACHIEVEMENT_002 | 400 | 잘못된 성취 키 |
| ACHIEVEMENT_003 | 400 | 이미 완료된 성취 |
| ACHIEVEMENT_004 | 400 | 보상이 이미 수령됨 |
| ACHIEVEMENT_005 | 400 | 완료되지 않은 성취 |

---

## 📝 사용 예시

### 학습 세션 완료 시
```javascript
// 1. 활동 기록
await fetch('/api/v1/analytics/activities/record?activityType=SESSION_COMPLETED&activityCategory=SESSION', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` }
});

// 2. 학습 진도 업데이트
await fetch('/api/v1/analytics/learning-progress/update?languageCode=en&progressType=SESSION_COMPLETED&value=1', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` }
});

// 3. 성취 진행도 증가
await fetch('/api/v1/achievements/progress/increment?achievementKey=session_count', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` }
});

// 4. 성취 완료 확인
const completedAchievements = await fetch('/api/v1/achievements/check-completion', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### 대시보드 데이터 로딩
```javascript
// 사용자 통계 조회
const userStats = await fetch('/api/v1/analytics/users/my-stats', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 성취 현황 조회  
const achievements = await fetch('/api/v1/achievements/my', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 성취 통계 조회
const achievementStats = await fetch('/api/v1/achievements/my/stats', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

---

## 🔗 관련 문서

- [메인 API 레퍼런스](./api-reference.md)
- [매칭 시스템 API](./matching-api.md)
- [데이터베이스 스키마](../05-database/database-schema.md)
- [백엔드 서비스 개요](../07-backend/services-overview.md)