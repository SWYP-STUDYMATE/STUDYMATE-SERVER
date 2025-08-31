# 🤝 매칭 시스템 API 레퍼런스

## 📅 문서 정보
- **최종 업데이트**: 2025-08-30
- **작성자**: Backend Development Team  
- **목적**: 확장된 매칭 시스템 API 문서화
- **베이스 URL**: `https://api.languagemate.kr`

---

## 🎯 매칭 시스템 개요

STUDYMATE의 매칭 시스템은 다음과 같은 기능을 제공합니다:
- **기본 파트너 추천**: 언어, 나이, 레벨 기반 필터링
- **고급 필터링**: 성격, 관심사, 스케줄 등 세부 조건
- **AI 스마트 매칭**: 사용자 행동 패턴 학습 기반 추천
- **실시간 매칭**: 온라인 사용자 대상 즉시 매칭
- **스케줄 기반 매칭**: 시간대/요일 기반 매칭
- **언어 교환 매칭**: 상호 언어 학습 가능한 파트너

---

## 🔍 파트너 검색 및 추천

### 기본 파트너 추천 조회
기본적인 필터를 사용하여 파트너를 추천합니다.

**GET** `/api/v1/matching/partners`

#### 쿼리 파라미터
- `nativeLanguage` (optional): 모국어 필터
- `targetLanguage` (optional): 학습 언어 필터  
- `languageLevel` (optional): 언어 레벨 필터
- `minAge` (optional): 최소 나이
- `maxAge` (optional): 최대 나이
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

#### 예시 요청
```
GET /api/v1/matching/partners?nativeLanguage=English&targetLanguage=Korean&languageLevel=INTERMEDIATE&minAge=20&maxAge=30
```

#### 응답
```json
{
  "content": [
    {
      "userId": "user-uuid-456",
      "name": "John Smith",
      "profileImage": "https://...",
      "age": 25,
      "compatibilityScore": 95.5,
      "matchingReasons": ["같은 시간대 활동", "유사한 학습 목표"],
      "languageInfo": {
        "nativeLanguage": "English",
        "learningLanguage": "Korean", 
        "level": "ADVANCED"
      },
      "location": {
        "city": "Seoul",
        "country": "Korea"
      },
      "personalityType": "OUTGOING",
      "commonInterests": ["Travel", "Technology"],
      "onlineStatus": "ONLINE",
      "lastActiveAt": "2025-08-30T10:30:00Z",
      "averageRating": 4.8,
      "totalSessions": 124
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "hasNext": true,
  "number": 0,
  "size": 20
}
```

### 고급 필터를 사용한 파트너 추천
다양한 필터 조건을 사용하여 최적화된 파트너를 추천합니다.

**POST** `/api/v1/matching/partners/advanced`

#### 요청 바디
```json
{
  "languageFilters": {
    "learningLanguage": "ENGLISH",
    "nativeLanguage": "KOREAN", 
    "minimumLevel": "INTERMEDIATE",
    "maximumLevel": "ADVANCED"
  },
  "personalityFilters": {
    "preferredPersonalities": ["OUTGOING", "PATIENT"],
    "communicationStyle": "CASUAL",
    "teachingStyle": "ENCOURAGING"
  },
  "availabilityFilters": {
    "dayOfWeek": "MONDAY",
    "timeSlot": "19:00-21:00",
    "timezone": "Asia/Seoul",
    "flexibleSchedule": true
  },
  "compatibilityFilters": {
    "ageRange": {"min": 20, "max": 35},
    "sharedInterests": ["TRAVEL", "MOVIES", "TECHNOLOGY"],
    "studyGoals": ["BUSINESS_ENGLISH", "CONVERSATION_PRACTICE"],
    "experienceLevel": "INTERMEDIATE"
  },
  "geographicFilters": {
    "country": "Korea",
    "city": "Seoul",
    "timezoneTolerance": 3
  },
  "activityFilters": {
    "onlineOnly": true,
    "minimumRating": 4.0,
    "maximumResponseTime": 24,
    "activeWithinHours": 48
  },
  "sortBy": "compatibility",
  "sortDirection": "desc",
  "limit": 20,
  "excludeUserIds": ["user-uuid-123", "user-uuid-789"]
}
```

### 온라인 파트너 추천
현재 온라인 상태인 사용자들 중에서 파트너를 추천합니다.

**POST** `/api/v1/matching/partners/online`

#### 요청 바디 (선택사항)
```json
{
  "onlineOnly": true,
  "nativeLanguage": "English",
  "city": "Seoul",
  "sortBy": "lastactive",
  "sortDirection": "desc",
  "limit": 10
}
```

### 즉석 매칭
현재 온라인이고 즉시 매칭 가능한 파트너를 찾습니다.

**GET** `/api/v1/matching/partners/instant`

#### 쿼리 파라미터
- `nativeLanguage` (optional): 모국어 필터
- `city` (optional): 도시 필터

#### 응답
즉석 매칭 결과는 최대 10명으로 제한되며, 현재 온라인이고 최근 활동한 사용자 순으로 정렬됩니다.

---

## 🤖 AI 기반 스마트 매칭

### AI 스마트 매칭
사용자 행동 패턴과 선호도를 학습하여 최적화된 파트너를 추천합니다.

**GET** `/api/v1/matching/smart-recommendations`

#### 쿼리 파라미터
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

#### 응답
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": "user-uuid-456",
        "aiMatchingScore": 97.3,
        "personalizedReasons": [
          "유사한 학습 패턴 감지",
          "선호하는 대화 주제 일치",
          "성공적인 세션 기록 보유"
        ],
        "behaviorCompatibility": {
          "sessionDurationMatch": 0.95,
          "communicationStyleMatch": 0.89,
          "learningPaceMatch": 0.92
        },
        "predictedSessionSuccess": 0.94
      }
    ]
  },
  "message": "AI 기반 스마트 매칭 추천을 성공적으로 조회했습니다."
}
```

### 실시간 매칭
현재 온라인인 사용자들 중에서 즉시 매칭 가능한 파트너를 찾습니다.

**GET** `/api/v1/matching/real-time`

#### 쿼리 파라미터
- `sessionType` (optional): 세션 타입 (기본값: ANY)
  - 가능한 값: VIDEO, AUDIO, CHAT, ANY

#### 응답
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": "user-uuid-789",
        "availableNow": true,
        "estimatedWaitTime": 30,
        "sessionTypes": ["VIDEO", "AUDIO"],
        "currentActivity": "LOOKING_FOR_PARTNER",
        "lastActiveAt": "2025-08-30T10:25:00Z"
      }
    ]
  },
  "message": "실시간 매칭 파트너를 성공적으로 조회했습니다."
}
```

---

## 📬 매칭 요청 관리

### 매칭 요청 보내기
특정 사용자에게 매칭 요청을 보냅니다.

**POST** `/api/v1/matching/request`

#### 요청 바디
```json
{
  "targetUserId": "user-uuid-456",
  "message": "안녕하세요! 언어 교환을 함께 해요!",
  "preferredSchedule": {
    "dayOfWeek": "MONDAY", 
    "timeSlot": "19:00-21:00"
  },
  "sessionType": "VIDEO"
}
```

### 보낸 매칭 요청 목록
사용자가 보낸 매칭 요청 목록을 조회합니다.

**GET** `/api/v1/matching/requests/sent`

#### 쿼리 파라미터
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

#### 응답
```json
{
  "content": [
    {
      "requestId": "request-uuid-123",
      "targetUser": {
        "userId": "user-uuid-456", 
        "name": "John Smith",
        "profileImage": "https://..."
      },
      "message": "안녕하세요! 언어 교환을 함께 해요!",
      "status": "PENDING",
      "sentAt": "2025-08-30T09:15:00Z",
      "expiresAt": "2025-09-06T09:15:00Z"
    }
  ]
}
```

### 받은 매칭 요청 목록
사용자가 받은 매칭 요청 목록을 조회합니다.

**GET** `/api/v1/matching/requests/received`

#### 응답
```json
{
  "content": [
    {
      "requestId": "request-uuid-789",
      "fromUser": {
        "userId": "user-uuid-456",
        "name": "John Smith", 
        "profileImage": "https://...",
        "compatibilityScore": 95.5
      },
      "message": "안녕하세요! 언어 교환을 함께 해요!",
      "status": "PENDING",
      "receivedAt": "2025-08-30T10:30:00Z",
      "expiresAt": "2025-09-06T10:30:00Z"
    }
  ]
}
```

### 매칭 요청 수락
**POST** `/api/v1/matching/accept/{requestId}`

### 매칭 요청 거절  
**POST** `/api/v1/matching/reject/{requestId}`

---

## 👥 매칭된 파트너 관리

### 매칭된 파트너 목록
현재 매칭된 파트너들의 목록을 조회합니다.

**GET** `/api/v1/matching/matches`

#### 응답
```json
{
  "content": [
    {
      "matchId": "match-uuid-123",
      "partner": {
        "userId": "user-uuid-456",
        "name": "John Smith",
        "profileImage": "https://..."
      },
      "matchedAt": "2025-08-25T10:00:00Z",
      "sessionCount": 5,
      "lastSessionAt": "2025-08-29T14:00:00Z",
      "averageRating": 4.8,
      "relationshipStatus": "ACTIVE",
      "totalChatMessages": 156
    }
  ]
}
```

### 매칭 제거
특정 매칭을 제거합니다.

**DELETE** `/api/v1/matching/matches/{matchId}`

### 호환성 점수 조회
특정 사용자와의 호환성 점수를 조회합니다.

**GET** `/api/v1/matching/compatibility/{targetUserId}`

#### 응답
```json
{
  "userId": "user-uuid-456",
  "compatibilityScore": 95.5,
  "scoreBreakdown": {
    "languageCompatibility": 98.0,
    "scheduleCompatibility": 85.0,
    "personalityCompatibility": 92.0,
    "interestCompatibility": 87.5,
    "experienceCompatibility": 94.0
  },
  "strengths": [
    "언어 수준이 매우 잘 맞음",
    "학습 목표가 유사함",
    "활동 시간대가 일치함"
  ],
  "considerations": [
    "나이 차이가 조금 있음",
    "일부 관심사가 다름"
  ]
}
```

---

## 🕒 스케줄 기반 매칭

### 스케줄 기반 매칭
특정 시간대/요일 기반으로 파트너를 매칭합니다.

**GET** `/api/v1/matching/schedule-based`

#### 쿼리 파라미터
- `dayOfWeek` (required): 요일
  - 가능한 값: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
- `timeSlot` (required): 시간대 (예: "19:00-21:00")
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

#### 응답
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": "user-uuid-456",
        "availableSchedule": {
          "dayOfWeek": "MONDAY",
          "timeSlots": ["19:00-21:00", "21:00-23:00"]
        },
        "timezone": "Asia/Seoul",
        "flexibilityScore": 0.8,
        "regularSessionTimes": [
          {
            "dayOfWeek": "MONDAY",
            "timeSlot": "19:00-21:00",
            "frequency": "WEEKLY"
          }
        ]
      }
    ]
  },
  "message": "스케줄 기반 매칭 파트너를 성공적으로 조회했습니다."
}
```

### 언어 교환 매칭
서로의 언어를 배울 수 있는 파트너를 매칭합니다.

**GET** `/api/v1/matching/language-exchange`

#### 응답  
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": "user-uuid-456",
        "languageExchange": {
          "canTeach": "English",
          "wantsToLearn": "Korean",
          "myNativeLanguage": "Korean",
          "myLearningLanguage": "English",
          "exchangeCompatibility": 0.95
        },
        "mutualBenefit": {
          "teachingExperience": "INTERMEDIATE",
          "learningMotivation": "HIGH",
          "patienceLevel": "HIGH"
        }
      }
    ]
  },
  "message": "언어 교환 파트너를 성공적으로 조회했습니다."
}
```

---

## 🎯 매칭 대기열 시스템

### 매칭 대기열 참가
매칭 대기열에 참가하여 자동 매칭을 기다립니다.

**POST** `/api/v1/matching/queue/join`

#### 쿼리 파라미터
- `sessionType` (optional): 세션 타입 (기본값: ANY)

#### 응답
```json
{
  "success": true,
  "data": "매칭 대기열에 성공적으로 참가했습니다."
}
```

### 매칭 대기열 탈퇴
**POST** `/api/v1/matching/queue/leave`

### 매칭 대기열 상태 조회
현재 매칭 대기열 상태를 조회합니다.

**GET** `/api/v1/matching/queue/status`

#### 응답
```json
{
  "success": true, 
  "data": {
    "isInQueue": true,
    "queuePosition": 3,
    "estimatedWaitTime": 120,
    "joinedAt": "2025-08-30T10:30:00Z",
    "sessionType": "VIDEO",
    "queueStats": {
      "totalInQueue": 15,
      "averageWaitTime": 180,
      "matchSuccessRate": 0.85
    }
  },
  "message": "매칭 대기열 상태를 성공적으로 조회했습니다."
}
```

---

## 📊 매칭 피드백 및 분석

### 매칭 품질 피드백
매칭된 파트너에 대한 피드백을 제출합니다.

**POST** `/api/v1/matching/feedback`

#### 요청 바디
```json
{
  "partnerId": "user-uuid-456",
  "qualityScore": 4.5,
  "feedback": "Great conversation partner! Very patient and helpful.",
  "categories": {
    "communication": 5,
    "patience": 5, 
    "knowledge": 4,
    "punctuality": 4,
    "friendliness": 5
  },
  "wouldRecommend": true,
  "improvementSuggestions": "Could be more prepared with topics"
}
```

### 매칭 선호도 업데이트
사용자의 매칭 선호도를 업데이트합니다.

**PUT** `/api/v1/matching/preferences`

#### 요청 바디
```json
{
  "languageFilters": {
    "preferredNativeLanguages": ["English", "Japanese"],
    "avoidLanguages": ["Spanish"]
  },
  "personalityPreferences": {
    "preferredPersonalities": ["PATIENT", "OUTGOING"],
    "avoidPersonalities": ["IMPATIENT"]
  },
  "sessionPreferences": {
    "preferredSessionTypes": ["VIDEO", "AUDIO"],
    "preferredDuration": 60,
    "flexibleSchedule": true
  },
  "geographicPreferences": {
    "preferSameTimezone": true,
    "maxTimezoneOffset": 3
  }
}
```

### 매칭 통계 조회
사용자의 매칭 통계를 조회합니다.

**GET** `/api/v1/matching/stats`

#### 응답
```json
{
  "success": true,
  "data": {
    "totalMatches": 25,
    "activeMatches": 8,
    "completedSessions": 156,
    "averageSessionRating": 4.7,
    "totalRequestsSent": 45,
    "totalRequestsReceived": 67,
    "acceptanceRate": 0.78,
    "responseRate": 0.92,
    "favoritePartners": [
      {
        "userId": "user-uuid-456",
        "sessionCount": 12,
        "averageRating": 4.9
      }
    ],
    "learningProgress": {
      "monthlyHours": 28.5,
      "improvementRate": 0.15
    }
  },
  "message": "매칭 통계를 성공적으로 조회했습니다."
}
```

### 매칭 알고리즘 성능 분석
**GET** `/api/v1/matching/analytics`

#### 응답
```json
{
  "success": true,
  "data": {
    "algorithmVersion": "v2.1",
    "accuracy": {
      "compatibilityPrediction": 0.87,
      "sessionSuccessRate": 0.82,
      "userSatisfactionScore": 4.6
    },
    "personalizedInsights": [
      "최근 AI 추천 정확도가 15% 향상되었습니다",
      "비슷한 관심사를 가진 파트너와의 세션 만족도가 높습니다"
    ],
    "recommendations": [
      "더 다양한 언어 수준의 파트너와 매칭해보세요",
      "새로운 시간대에 활동해보세요"
    ]
  },
  "message": "매칭 알고리즘 분석 결과를 성공적으로 조회했습니다."
}
```

### 매칭 알고리즘 최적화
매칭 이력을 기반으로 알고리즘을 최적화합니다.

**POST** `/api/v1/matching/optimize`

#### 응답
```json
{
  "success": true,
  "data": "매칭 알고리즘이 성공적으로 최적화되었습니다."
}
```

---

## 🚨 에러 코드

### 매칭 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| MATCHING_001 | 404 | 사용자를 찾을 수 없음 |
| MATCHING_002 | 400 | 잘못된 필터 조건 |
| MATCHING_003 | 400 | 이미 매칭된 사용자 |
| MATCHING_004 | 400 | 매칭 요청 만료 |
| MATCHING_005 | 400 | 본인에게 요청 불가 |
| MATCHING_006 | 403 | 차단된 사용자 |
| MATCHING_007 | 429 | 요청 한도 초과 |

### 대기열 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| QUEUE_001 | 400 | 이미 대기열에 참가 중 |
| QUEUE_002 | 404 | 대기열에 없음 |
| QUEUE_003 | 503 | 대기열 서비스 오류 |

---

## 🔗 관련 문서

- [메인 API 레퍼런스](./api-reference.md)
- [Analytics & Achievement API](./analytics-achievement-api.md) 
- [WebSocket API](./websocket-api.md)
- [데이터베이스 스키마](../05-database/database-schema.md)
- [백엔드 서비스 개요](../07-backend/services-overview.md)