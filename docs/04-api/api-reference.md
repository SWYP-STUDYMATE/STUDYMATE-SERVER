# 📚 STUDYMATE API 레퍼런스

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Backend Development Team
- **목적**: STUDYMATE REST API 엔드포인트 및 사용법 가이드
- **API 버전**: v1
- **베이스 URL**: `https://api.languagemate.kr/api/v1`

---

## 🔐 인증

### JWT 토큰 기반 인증
모든 보호된 엔드포인트는 Authorization 헤더에 JWT 토큰이 필요합니다.

```http
Authorization: Bearer <access_token>
```

### 토큰 갱신
Access token이 만료되면 refresh token을 사용하여 새로운 토큰을 발급받을 수 있습니다.

---

## 🏗️ API 구조

### HTTP 상태 코드
| 상태 코드 | 의미 | 설명 |
|----------|------|------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 500 | Internal Server Error | 서버 내부 오류 |

### 표준 응답 형식
```json
{
  "success": true,
  "data": { ... },
  "message": "Success"
}
```

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message"
  }
}
```

---

## 🔑 인증 API

### 네이버 로그인 URL 조회
소셜 로그인을 위한 OAuth URL을 반환합니다.

**GET** `/api/v1/login/naver`

#### 응답
```json
{
  "success": true,
  "data": {
    "loginUrl": "https://nid.naver.com/oauth2.0/authorize?..."
  }
}
```

### 구글 로그인 URL 조회
**GET** `/api/v1/login/google`

#### 응답
```json
{
  "success": true,
  "data": {
    "loginUrl": "https://accounts.google.com/oauth/authorize?..."
  }
}
```

### OAuth 콜백 처리
OAuth 인증 후 콜백을 처리하여 JWT 토큰을 발급합니다.

**GET** `/login/oauth2/code/naver`
**GET** `/login/oauth2/code/google`

#### 쿼리 파라미터
- `code`: OAuth 인증 코드
- `state`: CSRF 방지를 위한 상태값

#### 응답
사용자를 프론트엔드로 리다이렉트하며 토큰을 쿼리 파라미터로 전달합니다.

### 토큰 갱신
Access token 갱신을 위한 엔드포인트입니다.

**POST** `/api/v1/auth/refresh`

#### 헤더
```http
Authorization: Bearer <refresh_token>
```

#### 응답
```json
{
  "success": true,
  "data": {
    "accessToken": "new_access_token",
    "refreshToken": "new_refresh_token"
  }
}
```

---

## 👤 사용자 관리 API

### 사용자 이름 조회
**GET** `/api/v1/user/name`

#### 응답
```json
{
  "success": true,
  "data": {
    "name": "홍길동",
    "englishName": "John Doe"
  }
}
```

### 프로필 이미지 URL 조회
**GET** `/api/v1/user/profile`

#### 응답
```json
{
  "success": true,
  "data": {
    "profileImageUrl": "https://storage.example.com/profiles/user123.jpg"
  }
}
```

### 영어 이름 저장
**POST** `/api/v1/user/english-name`

#### 요청 바디
```json
{
  "englishName": "John Doe"
}
```

### 출생연도 저장
**POST** `/api/v1/user/birthyear`

#### 요청 바디
```json
{
  "birthYear": 1990
}
```

### 생일 저장
**POST** `/api/v1/user/birthday`

#### 요청 바디
```json
{
  "birthDay": "03-15"
}
```

### 프로필 이미지 업로드
**POST** `/api/v1/user/profile-image`

#### Content-Type
```
multipart/form-data
```

#### 요청 파라미터
- `file`: 이미지 파일 (최대 10MB)

### 성별 저장
**POST** `/api/v1/user/gender`

#### 요청 바디
```json
{
  "genderType": "MALE" // MALE, FEMALE, OTHER
}
```

### 자기소개 저장
**POST** `/api/v1/user/self-bio`

#### 요청 바디
```json
{
  "selfBio": "안녕하세요. 언어교환을 통해 영어 실력을 향상시키고 싶습니다."
}
```

### 위치 정보 저장
**POST** `/api/v1/user/location`

#### 요청 바디
```json
{
  "locationId": 1
}
```

### 전체 위치 목록 조회
**GET** `/api/v1/user/locations`

#### 응답
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "서울특별시",
      "code": "SEOUL"
    },
    {
      "id": 2,
      "name": "부산광역시", 
      "code": "BUSAN"
    }
  ]
}
```

### 성별 타입 목록 조회
**GET** `/api/v1/user/gender-type`

#### 응답
```json
{
  "success": true,
  "data": [
    {
      "type": "MALE",
      "displayName": "남성"
    },
    {
      "type": "FEMALE",
      "displayName": "여성"
    }
  ]
}
```

---

## 📝 온보딩 API

### 언어 설정 저장
사용자의 학습하고자 하는 언어와 현재 수준을 저장합니다.

**POST** `/api/v1/onboarding/language`

#### 요청 바디
```json
{
  "learningLanguages": [
    {
      "languageId": 1,
      "levelId": 3
    }
  ],
  "nativeLanguageId": 2
}
```

### 관심사 저장
**POST** `/api/v1/onboarding/interests`

#### 요청 바디
```json
{
  "motivationIds": [1, 3, 5],
  "learningStyleIds": [2, 4],
  "communicationMethodIds": [1, 2]
}
```

### 파트너 선호도 저장
**POST** `/api/v1/onboarding/partner`

#### 요청 바디
```json
{
  "preferredGenders": ["MALE", "FEMALE"],
  "preferredPersonalityTypes": ["EXTROVERT", "INTROVERT"],
  "ageRange": {
    "min": 20,
    "max": 30
  }
}
```

### 스케줄 저장
**POST** `/api/v1/onboarding/schedule`

#### 요청 바디
```json
{
  "availableSchedules": [
    {
      "dayOfWeek": "MONDAY",
      "timeSlots": ["09:00-12:00", "14:00-18:00"]
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "timeSlots": ["19:00-22:00"]
    }
  ]
}
```

---

## 🎯 레벨 테스트 API

### 레벨 테스트 시작
**POST** `/api/v1/level-test/start`

#### 응답
```json
{
  "success": true,
  "data": {
    "testId": "test-uuid-123",
    "questions": [
      {
        "id": 1,
        "type": "SPEAKING",
        "question": "Please introduce yourself.",
        "timeLimit": 60
      }
    ]
  }
}
```

### 음성 답변 제출
**POST** `/api/v1/level-test/submit`

#### Content-Type
```
multipart/form-data
```

#### 요청 파라미터
- `testId`: 테스트 ID
- `questionId`: 문제 ID
- `audioFile`: 음성 파일

### 테스트 결과 조회
**GET** `/api/v1/level-test/result/{testId}`

#### 응답
```json
{
  "success": true,
  "data": {
    "testId": "test-uuid-123",
    "overallLevel": "B2",
    "scores": {
      "pronunciation": 85,
      "fluency": 78,
      "vocabulary": 82,
      "grammar": 80
    },
    "feedback": "전반적으로 중상급 수준입니다...",
    "recommendations": [
      "문법 실수를 줄이기 위해 더 많은 연습이 필요합니다.",
      "발음이 명확하고 이해하기 쉽습니다."
    ]
  }
}
```

---

## 💬 채팅 API

### 채팅방 목록 조회
**GET** `/api/v1/chat/rooms`

#### 응답
```json
{
  "success": true,
  "data": {
    "rooms": [
      {
        "id": "room-uuid-123",
        "name": "John과의 채팅",
        "lastMessage": "안녕하세요!",
        "lastMessageTime": "2025-08-27T10:30:00Z",
        "unreadCount": 3,
        "participants": [
          {
            "id": "user-uuid-456",
            "name": "John",
            "profileImageUrl": "https://..."
          }
        ]
      }
    ]
  }
}
```

### 채팅방 생성
**POST** `/api/v1/chat/rooms`

#### 요청 바디
```json
{
  "participantIds": ["user-uuid-456"],
  "roomName": "English Practice Room"
}
```

### 채팅 메시지 조회
**GET** `/api/v1/chat/rooms/{roomId}/messages`

#### 쿼리 파라미터
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 50)

#### 응답
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "id": "msg-uuid-789",
        "senderId": "user-uuid-123",
        "senderName": "홍길동",
        "content": "안녕하세요!",
        "messageType": "TEXT",
        "timestamp": "2025-08-27T10:30:00Z",
        "isRead": true
      }
    ],
    "hasNext": false
  }
}
```

---

## 🎥 세션 관리 API

### 세션 생성
**POST** `/api/v1/sessions`

#### 요청 바디
```json
{
  "type": "VIDEO", // VIDEO, AUDIO
  "participantIds": ["user-uuid-456"],
  "scheduledTime": "2025-08-27T14:00:00Z",
  "duration": 3600,
  "title": "English Conversation Practice"
}
```

### 세션 목록 조회
**GET** `/api/v1/sessions`

#### 쿼리 파라미터
- `status`: 세션 상태 (`SCHEDULED`, `ACTIVE`, `COMPLETED`)
- `date`: 날짜 필터 (YYYY-MM-DD)

### 세션 참여
**POST** `/api/v1/sessions/{sessionId}/join`

#### 응답
```json
{
  "success": true,
  "data": {
    "webrtcToken": "webrtc-token-123",
    "iceServers": [
      {
        "urls": ["stun:stun.l.google.com:19302"]
      }
    ]
  }
}
```

---

## 🤖 AI 기능 API

### Clova Studio 텍스트 교정
**POST** `/api/v1/clova/correct`

#### 요청 바디
```json
{
  "text": "I am goes to school everyday.",
  "language": "en"
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "originalText": "I am goes to school everyday.",
    "correctedText": "I go to school every day.",
    "corrections": [
      {
        "original": "am goes",
        "corrected": "go",
        "reason": "주어가 'I'일 때는 동사 원형을 사용합니다."
      }
    ]
  }
}
```

---

## 📊 통계 및 분석 API

### 학습 통계 조회
**GET** `/api/v1/analytics/stats`

#### 쿼리 파라미터
- `period`: 기간 (`WEEK`, `MONTH`, `YEAR`)

#### 응답
```json
{
  "success": true,
  "data": {
    "totalSessions": 15,
    "totalDuration": 18000,
    "averageSessionDuration": 1200,
    "completedLevelTests": 3,
    "currentLevel": "B2",
    "weeklyProgress": [
      {
        "date": "2025-08-20",
        "sessions": 2,
        "duration": 2400
      }
    ]
  }
}
```

### 매칭 기록 조회
**GET** `/api/v1/matching/history`

#### 응답
```json
{
  "success": true,
  "data": {
    "matches": [
      {
        "partnerId": "user-uuid-456",
        "partnerName": "John",
        "matchedDate": "2025-08-25T10:00:00Z",
        "sessionCount": 5,
        "lastSessionDate": "2025-08-27T14:00:00Z"
      }
    ]
  }
}
```

---

## 🔔 알림 API

### 알림 목록 조회
**GET** `/api/v1/notifications`

#### 응답
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": "notif-uuid-123",
        "type": "SESSION_REMINDER",
        "title": "세션 시작 알림",
        "message": "John과의 영어 회화 세션이 10분 후 시작됩니다.",
        "isRead": false,
        "createdAt": "2025-08-27T13:50:00Z"
      }
    ],
    "unreadCount": 5
  }
}
```

### 알림 읽음 처리
**PUT** `/api/v1/notifications/{notificationId}/read`

---

## 🔧 시스템 API

### 헬스 체크
**GET** `/actuator/health`

#### 응답
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 시스템 정보
**GET** `/actuator/info`

#### 응답
```json
{
  "app": {
    "name": "studymate-server",
    "version": "1.0.0",
    "description": "STUDYMATE Backend API"
  }
}
```

---

## 🌐 WebSocket API

### 연결 엔드포인트
```
wss://api.languagemate.kr/ws
```

### STOMP 구독 및 발행

#### 채팅 메시지 구독
```javascript
stompClient.subscribe('/topic/chatroom/{roomId}', function(message) {
  const chatMessage = JSON.parse(message.body);
  // 메시지 처리 로직
});
```

#### 메시지 전송
```javascript
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  roomId: 'room-uuid-123',
  content: '안녕하세요!',
  messageType: 'TEXT'
}));
```

#### 세션 상태 구독
```javascript
stompClient.subscribe('/topic/session/{sessionId}', function(message) {
  const sessionUpdate = JSON.parse(message.body);
  // 세션 상태 업데이트 처리
});
```

---

## 📝 에러 코드

### 인증 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| AUTH_001 | 401 | 유효하지 않은 토큰 |
| AUTH_002 | 401 | 토큰 만료 |
| AUTH_003 | 403 | 권한 없음 |

### 사용자 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| USER_001 | 404 | 사용자를 찾을 수 없음 |
| USER_002 | 400 | 잘못된 사용자 정보 |

### 파일 업로드 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| FILE_001 | 400 | 지원하지 않는 파일 형식 |
| FILE_002 | 413 | 파일 크기 초과 (최대 10MB) |

### 채팅 관련
| 코드 | HTTP | 설명 |
|------|------|------|
| CHAT_001 | 404 | 채팅방을 찾을 수 없음 |
| CHAT_002 | 403 | 채팅방 참여 권한 없음 |

---

## 📞 지원

### Swagger 문서
- **개발**: `http://localhost:8080/swagger-ui/index.html`
- **프로덕션**: `https://api.languagemate.kr/swagger-ui/index.html`

### 문제 신고
- GitHub Issues: [STUDYMATE-SERVER Issues](https://github.com/SWYP-STUDYMATE/STUDYMATE-SERVER/issues)
- Email: dev@studymate.kr