# 📚 STUDYMATE API 레퍼런스

## 📅 문서 정보
- **최종 업데이트**: 2025-09-18 (페이징 응답 구조 안정화)
- **작성자**: Backend Development Team
- **목적**: STUDYMATE REST API 엔드포인트 및 사용법 가이드
- **API 버전**: v1
- **베이스 URL**: `https://api.languagemate.kr/api/v1`

## ⚠️ 중요 업데이트 (2025-01-02)
**API 경로 표준화**: 모든 API 엔드포인트가 `/api/v1/` 프리픽스로 표준화되었습니다.
- ✅ **채팅 API**: `/api/chat/*` → `/api/v1/chat/*` 
- ✅ **사용자 API**: 기존 `/api/v1/user/*` 유지
- ✅ **온보딩 API**: 기존 `/api/v1/onboarding/*` 유지
- ✅ **인증 API**: 기존 `/api/v1/auth/*` 유지

**클라이언트 연동**: axios baseURL이 `/api/v1`로 설정되어 자동으로 프리픽스 추가됨

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

### 페이징 응답 구조 (2025-09-18)
Spring Data의 `PageImpl` 직렬화 불안정을 해소하기 위해 모든 페이징 API는 `PageResponse<T>` 구조를 반환합니다.

```json
{
  "success": true,
  "data": {
    "content": [ /* 도메인 데이터 배열 */ ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 125,
      "totalPages": 7,
      "numberOfElements": 20,
      "first": true,
      "last": false,
      "hasNext": true,
      "hasPrevious": false,
      "empty": false
    },
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false,
      "orders": [
        {
          "property": "createdAt",
          "direction": "DESC",
          "ignoreCase": false,
          "nullHandling": "NATIVE"
        }
      ]
    }
  },
  "message": "..."
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

### 사용자 온라인 상태 조회
**GET** `/api/v1/user/status/{userId}`

#### 응답
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid-123",
    "status": "ONLINE",
    "lastSeenAt": "2025-08-29T10:30:00Z",
    "currentActivity": "STUDYING",
    "deviceInfo": "Chrome on MacOS"
  }
}
```

### 내 온라인 상태 업데이트
**POST** `/api/v1/user/status/update`

#### 요청 바디
```json
{
  "status": "ONLINE",
  "deviceInfo": "Chrome on MacOS",
  "activity": "STUDYING"
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

### ✨ 온보딩 UX 개선 API

### 단계별 온보딩 데이터 저장
사용자가 온보딩을 진행하면서 각 단계별로 데이터를 저장합니다.

**POST** `/api/v1/onboarding/steps/{stepNumber}/save`

#### 요청 바디
```json
{
  "stepNumber": 3,
  "stepData": {
    "learningLanguage": "English",
    "languageLevel": "Intermediate",
    "motivation": ["Career", "Travel"]
  }
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "stepNumber": 3,
    "progressPercentage": 42.8,
    "isCompleted": false,
    "nextStepInfo": {
      "stepNumber": 4,
      "title": "파트너 선호도 설정",
      "description": "원하는 언어교환 파트너의 특성을 선택해주세요"
    }
  },
  "message": "3단계 데이터가 저장되었습니다"
}
```

### 현재 진행 중인 단계 조회
**GET** `/api/v1/onboarding/steps/current`

#### 응답
```json
{
  "success": true,
  "data": {
    "currentStep": 3,
    "totalSteps": 7,
    "progressPercentage": 42.8,
    "completedSteps": [1, 2],
    "stepData": {
      "1": {"name": "홍길동", "englishName": "John"},
      "2": {"learningLanguage": "English"}
    },
    "motivationalMessage": "벌써 절반 가까이 완료했어요! 조금만 더 힘내세요 💪"
  }
}
```

### 온보딩 단계 건너뛰기
**POST** `/api/v1/onboarding/steps/{stepNumber}/skip`

#### 응답
```json
{
  "success": true,
  "data": {
    "skippedStep": 4,
    "nextStep": 5,
    "progressPercentage": 57.1,
    "canSkip": true
  },
  "message": "4단계를 건너뛰었습니다"
}
```

### 자동 저장 (백그라운드)
**POST** `/api/v1/onboarding/auto-save`

#### 요청 바디
```json
{
  "currentStep": 3,
  "formData": {
    "partialInput": "현재 입력 중인 데이터"
  },
  "timestamp": 1629789600000
}
```

### 임시 매칭 체험
온보딩 중에 매칭 시스템을 미리 체험해볼 수 있습니다.

**POST** `/api/v1/onboarding/trial-matching`

#### 요청 바디
```json
{
  "preferences": {
    "learningLanguage": "English",
    "languageLevel": "Intermediate"
  }
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "trialPartners": [
      {
        "name": "Alex (체험용)",
        "profileImage": "https://example.com/alex.jpg",
        "compatibilityScore": 87.5,
        "commonInterests": ["Travel", "Movies"],
        "description": "실제 매칭 시스템 체험"
      }
    ],
    "estimatedWaitTime": "평균 2-3분",
    "totalAvailablePartners": 156
  }
}
```

### 온보딩 진행률 조회
**GET** `/api/v1/onboarding/progress`

#### 응답
```json
{
  "success": true,
  "data": {
    "progressPercentage": 71.4,
    "completedSteps": 5,
    "totalSteps": 7,
    "timeSpent": "12분",
    "estimatedTimeRemaining": "3-5분",
    "milestones": [
      {
        "step": 5,
        "title": "절반 완료!",
        "reward": "첫 단계 완료 배지"
      }
    ]
  }
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

## 🤝 매칭 시스템 API

### 매칭 파트너 검색 (고급 필터링)
**POST** `/api/v1/matching/search`

#### 요청 바디
```json
{
  "languageFilters": {
    "learningLanguage": "ENGLISH",
    "nativeLanguage": "KOREAN",
    "minimumLevel": "INTERMEDIATE"
  },
  "personalityFilters": {
    "preferredPersonalities": ["OUTGOING", "PATIENT"],
    "communicationStyle": "CASUAL"
  },
  "availabilityFilters": {
    "dayOfWeek": "MONDAY",
    "timeSlot": "19:00-21:00",
    "timezone": "Asia/Seoul"
  },
  "compatibilityFilters": {
    "ageRange": {"min": 20, "max": 35},
    "sharedInterests": ["TRAVEL", "MOVIES"],
    "studyGoals": ["BUSINESS_ENGLISH"]
  },
  "page": 0,
  "size": 10
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "partners": [
      {
        "userId": "user-uuid-456",
        "name": "John Smith",
        "profileImage": "https://...",
        "compatibilityScore": 95.5,
        "matchingReasons": ["같은 시간대 활동", "유사한 학습 목표"],
        "languageInfo": {
          "nativeLanguage": "English",
          "learningLanguage": "Korean",
          "level": "ADVANCED"
        },
        "personalityType": "OUTGOING",
        "commonInterests": ["Travel", "Technology"]
      }
    ],
    "totalElements": 25,
    "hasNext": true
  }
}
```

### 매칭 요청 보내기
**POST** `/api/v1/matching/request`

#### 요청 바디
```json
{
  "targetUserId": "user-uuid-456",
  "message": "안녕하세요! 언어 교환을 함께 해요!",
  "preferredSchedule": {
    "dayOfWeek": "MONDAY",
    "timeSlot": "19:00-21:00"
  }
}
```

### 받은 매칭 요청 목록
**GET** `/api/v1/matching/requests/received`

#### 응답
```json
{
  "success": true,
  "data": {
    "requests": [
      {
        "requestId": "request-uuid-789",
        "fromUser": {
          "userId": "user-uuid-456",
          "name": "John Smith",
          "profileImage": "https://..."
        },
        "message": "안녕하세요! 언어 교환을 함께 해요!",
        "status": "PENDING",
        "createdAt": "2025-08-29T10:30:00Z",
        "compatibilityScore": 95.5
      }
    ]
  }
}
```

### 매칭 요청 응답 (수락/거절)
**POST** `/api/v1/matching/requests/{requestId}/respond`

#### 요청 바디
```json
{
  "response": "ACCEPTED", // ACCEPTED, REJECTED
  "message": "네, 좋아요! 함께 공부해요!"
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
        "isRead": true,
        "files": [],
        "images": []
      }
    ],
    "hasNext": false
  }
}
```

### 채팅 파일 업로드
**POST** `/api/v1/chat/files/upload`

#### Content-Type
```
multipart/form-data
```

#### 요청 파라미터
- `roomId`: 채팅방 ID
- `files`: 업로드할 파일들 (최대 10개, 각 파일 최대 50MB)
- `description`: 파일 설명 (선택사항)

#### 응답
```json
{
  "success": true,
  "data": {
    "uploadedFiles": [
      {
        "fileId": "file-uuid-123",
        "fileName": "document.pdf",
        "fileType": "DOCUMENT",
        "fileSize": 2048576,
        "downloadUrl": "https://api.languagemate.kr/api/v1/chat/files/file-uuid-123/download",
        "thumbnailUrl": null,
        "uploadedAt": "2025-08-29T10:30:00Z"
      }
    ]
  }
}
```

### 채팅 파일 다운로드
**GET** `/api/v1/chat/files/{fileId}/download`

#### 응답
파일 스트림이 직접 반환됩니다.

### 채팅 파일 정보 조회
**GET** `/api/v1/chat/files/{fileId}`

#### 응답
```json
{
  "success": true,
  "data": {
    "fileId": "file-uuid-123",
    "fileName": "document.pdf",
    "fileType": "DOCUMENT",
    "fileSize": 2048576,
    "uploadedBy": {
      "userId": "user-uuid-456",
      "name": "홍길동"
    },
    "uploadedAt": "2025-08-29T10:30:00Z",
    "downloadCount": 5,
    "isPublic": false
  }
}
```

### 메시지 읽음 처리
**POST** `/api/v1/chat/read-status/messages/{messageId}/read`

### 채팅방 메시지 일괄 읽음 처리
**POST** `/api/v1/chat/read-status/rooms/{roomId}/read-all`

### 안읽은 메시지 수 조회
**GET** `/api/v1/chat/read-status/rooms/{roomId}/unread-count`

#### 응답
```json
{
  "success": true,
  "data": 15
}
```

### 전체 안읽은 메시지 통계
**GET** `/api/v1/chat/read-status/global-unread-summary`

#### 응답
```json
{
  "success": true,
  "data": {
    "totalUnreadMessages": 42,
    "unreadRoomsCount": 5,
    "unreadByRoom": {
      "room-uuid-123": 15,
      "room-uuid-456": 12,
      "room-uuid-789": 8
    },
    "lastUpdatedAt": "2025-08-29T10:30:00Z"
  }
}
```

### 메시지 읽음 상태 조회
**GET** `/api/v1/chat/read-status/messages/{messageId}`

#### 응답
```json
{
  "success": true,
  "data": {
    "messageId": 123,
    "totalReaders": 2,
    "totalParticipants": 3,
    "readers": [
      {
        "userId": "user-uuid-456",
        "userName": "John Smith",
        "profileImage": "https://...",
        "readAt": "2025-08-29T10:30:00Z"
      }
    ],
    "unreadUserIds": ["user-uuid-789"],
    "isFullyRead": false,
    "readPercentage": 66.7
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

## 📹 WebRTC API

### WebRTC 룸 생성
**POST** `/api/v1/webrtc/rooms/{sessionId}`

#### 쿼리 파라미터
- `hostUserId`: 호스트 사용자 ID

#### 응답
```json
{
  "success": true,
  "data": {
    "roomId": "webrtc-room-uuid-123",
    "sessionId": 456,
    "status": "CREATED",
    "signalingServerUrl": "wss://signal.languagemate.kr",
    "iceServers": [
      {
        "urls": ["stun:stun.l.google.com:19302"],
        "username": null,
        "credential": null
      },
      {
        "urls": ["turn:turn.languagemate.kr:3478"],
        "username": "turnuser",
        "credential": "turnpass"
      }
    ],
    "maxParticipants": 2,
    "currentParticipants": 0,
    "isRecordingEnabled": false
  }
}
```

### WebRTC 룸 참가
**POST** `/api/v1/webrtc/rooms/{roomId}/join`

#### 요청 바디
```json
{
  "userId": "user-uuid-123",
  "peerId": "peer-123",
  "deviceInfo": "Chrome 120 on macOS",
  "cameraEnabled": true,
  "microphoneEnabled": true,
  "preferredVideoQuality": "HD"
}
```

#### 응답
```json
{
  "success": true,
  "data": {
    "participantId": "participant-uuid-789",
    "userId": "user-uuid-123",
    "peerId": "peer-123",
    "connectionStatus": "CONNECTING",
    "isHost": false,
    "isModerator": false,
    "cameraEnabled": true,
    "microphoneEnabled": true,
    "joinedAt": "2025-08-29T10:30:00Z"
  }
}
```

### WebRTC 룸 정보 조회
**GET** `/api/v1/webrtc/rooms/{roomId}`

#### 응답
```json
{
  "success": true,
  "data": {
    "roomId": "webrtc-room-uuid-123",
    "sessionId": 456,
    "status": "ACTIVE",
    "maxParticipants": 2,
    "currentParticipants": 2,
    "participants": [
      {
        "userId": "user-uuid-123",
        "userName": "홍길동",
        "peerId": "peer-123",
        "isHost": true,
        "connectionStatus": "CONNECTED",
        "cameraEnabled": true,
        "microphoneEnabled": true,
        "screenSharing": false,
        "joinedAt": "2025-08-29T10:30:00Z"
      }
    ],
    "startedAt": "2025-08-29T10:30:00Z"
  }
}
```

### 참가자 상태 업데이트
**PUT** `/api/v1/webrtc/rooms/{roomId}/participants/{userId}/status`

#### 쿼리 파라미터
- `statusType`: 상태 타입 (camera, microphone, screen_share)
- `statusValue`: 상태 값 (true/false)

### WebRTC 룸 종료
**POST** `/api/v1/webrtc/rooms/{roomId}/end`

#### 쿼리 파라미터
- `hostUserId`: 호스트 사용자 ID

### 녹화 시작/중지
**POST** `/api/v1/webrtc/rooms/{roomId}/recording/start`
**POST** `/api/v1/webrtc/rooms/{roomId}/recording/stop`

#### 쿼리 파라미터
- `userId`: 사용자 ID (호스트만 가능)

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

## 📊 분석 및 통계 대시보드 API

### 내 학습 통계 조회
**GET** `/api/v1/analytics/users/my-stats`

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
  }
}
```

### 기간별 학습 통계 조회
**GET** `/api/v1/analytics/users/my-stats/range`

#### 쿼리 파라미터
- `startDate`: 시작 날짜 (YYYY-MM-DD)
- `endDate`: 종료 날짜 (YYYY-MM-DD)

### 시스템 전체 분석 (관리자 전용)
**GET** `/api/v1/analytics/system`

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
    "activityTrend": [
      {
        "date": "2025-08-29T00:00:00",
        "value": 234,
        "metricName": "DAILY_ACTIVITY"
      }
    ],
    "activityByHour": {
      "9": 45,
      "10": 67,
      "19": 89,
      "20": 112
    },
    "topLanguages": [
      {
        "languageCode": "en",
        "languageName": "English",
        "userCount": 3200,
        "sessionCount": 8900,
        "averageProgress": 75.4
      }
    ],
    "systemHealth": {
      "successRate": 99.2,
      "averageResponseTime": 145.6,
      "errorCount": 12,
      "systemStatus": "HEALTHY"
    }
  }
}
```

### 사용자 활동 기록
**POST** `/api/v1/analytics/activities/record`

#### 쿼리 파라미터
- `activityType`: 활동 타입 (LOGIN, LOGOUT, SESSION_JOIN, MESSAGE_SENT 등)
- `activityCategory`: 활동 카테고리 (AUTH, SESSION, CHAT, PROFILE 등)
- `description`: 활동 설명 (선택사항)
- `metadata`: 추가 메타데이터 JSON (선택사항)

### 학습 진도 업데이트
**POST** `/api/v1/analytics/learning-progress/update`

#### 쿼리 파라미터
- `languageCode`: 언어 코드 (en, ko, ja 등)
- `progressType`: 진도 타입 (SESSION_COMPLETED, MESSAGE_SENT, WORDS_LEARNED 등)
- `value`: 값 (숫자)
- `metadata`: 추가 메타데이터 (선택사항)

### 시스템 메트릭 기록 (관리자 전용)
**POST** `/api/v1/analytics/metrics/record`

#### 쿼리 파라미터
- `metricName`: 메트릭 이름
- `metricCategory`: 메트릭 카테고리
- `metricValue`: 메트릭 값
- `metricUnit`: 측정 단위 (선택사항)
- `aggregationPeriod`: 집계 기간 (선택사항)

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
        "id": 42,
        "type": "session_reminder",
        "category": "session",
        "title": "세션 시작 알림",
        "message": "John과의 영어 회화 세션이 10분 후 시작됩니다.",
        "isRead": false,
        "status": "UNREAD",
        "priority": 2,
        "clickUrl": "/sessions/abcd-1234",
        "data": {
          "sessionId": "abcd-1234"
        },
        "createdAt": "2025-08-27T13:50:00",
        "readAt": null
      }
    ],
    "unreadCount": 5,
    "pagination": {
      "page": 1,
      "size": 20,
      "totalPages": 3,
      "totalElements": 54,
      "hasNext": true
    }
  }
}
```

### 알림 읽음 처리
**PATCH** `/api/v1/notifications/{notificationId}/read`

> 참고: 기존 `POST /api/v1/notifications/{notificationId}/read` 엔드포인트도 하위 호환을 위해 유지됩니다.

### 전체 알림 읽음 처리
**PATCH** `/api/v1/notifications/read-all`

### 알림 일괄 삭제
**DELETE** `/api/v1/notifications/batch`

#### 요청 바디
```json
{
  "notificationIds": [12, 13, 14]
}
```

### 알림 설정 조회/수정
**GET** `/api/v1/notifications/settings`

**PATCH** `/api/v1/notifications/settings`

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

#### WebRTC 시그널링
```javascript
// WebRTC 시그널링 메시지 구독
stompClient.subscribe('/topic/webrtc/{roomId}/signaling', function(message) {
  const signalingMessage = JSON.parse(message.body);
  // WebRTC 시그널링 처리 (offer, answer, ice-candidate)
});

// 시그널링 메시지 전송
stompClient.send('/app/webrtc/{roomId}/signaling', {}, JSON.stringify({
  type: 'offer',
  fromPeerId: 'peer-123',
  toPeerId: 'peer-456',
  data: sdpOffer
}));
```

#### 실시간 통계 구독
```javascript
// 사용자별 통계 업데이트 구독
stompClient.subscribe('/topic/users/{userId}/stats', function(message) {
  const statsUpdate = JSON.parse(message.body);
  // 실시간 통계 업데이트 처리
});

// 실시간 활동 피드백 구독
stompClient.subscribe('/topic/users/{userId}/feedback', function(message) {
  const feedback = JSON.parse(message.body);
  // XP 획득, 레벨업 등 피드백 처리
});

// 관리자 시스템 분석 구독
stompClient.subscribe('/topic/admin/analytics', function(message) {
  const analyticsUpdate = JSON.parse(message.body);
  // 관리자 대시보드 실시간 업데이트
});
```

#### 참가자 상태 업데이트
```javascript
// WebRTC 참가자 상태 업데이트 전송
stompClient.send('/app/webrtc/{roomId}/participant-update', {}, JSON.stringify({
  userId: 'user-uuid-123',
  statusType: 'camera',
  statusValue: false,
  timestamp: Date.now()
}));

// 참가자 업데이트 구독
stompClient.subscribe('/topic/webrtc/{roomId}/participant-updates', function(message) {
  const update = JSON.parse(message.body);
  // 다른 참가자의 상태 변경 처리
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

### WebRTC 룸 메타데이터 동기화 (신규)
**POST** `/api/v1/webrtc/rooms/{roomId}/sync`

- 목적: WebRTC 방에 세션 주제, 일정, 언어, 호스트 정보를 반영
- 인증: Bearer JWT (세션 호스트만 호출 가능)

#### 요청 바디
```json
{
  "sessionId": 456
}
```

#### 응답
```json
{
  "success": true,
  "message": "WebRTC 룸 메타데이터를 동기화했습니다."
}
```

> Spring은 위 요청을 수신하면 `PATCH /api/v1/internal/webrtc/rooms/{roomId}/metadata`를 Workers에 호출하여 메타데이터를 병합하고, 활성 룸 캐시를 갱신합니다.

### WebRTC 활성 룸 조회 (신규)
**GET** `/api/v1/webrtc/rooms/active`

- 설명: Workers에 생성된 활성 WebRTC 룸 정보를 세션 요약과 함께 반환
- 응답 데이터는 각 룸에 대해 `session` 객체(제목, 일정, 언어, 호스트 이름 등)를 포함합니다.

#### 응답 예시
```json
{
  "success": true,
  "data": [
    {
      "roomId": "room-123",
      "roomType": "video",
      "status": "active",
      "currentParticipants": 2,
      "maxParticipants": 4,
      "metadata": {
        "sessionId": 456,
        "title": "Grammar Workshop",
        "scheduledAt": "2025-01-15T10:00:00"
      },
      "session": {
        "sessionId": 456,
        "title": "Grammar Workshop",
        "description": "중급 문법 집중 세션",
        "scheduledAt": "2025-01-15T10:00:00",
        "durationMinutes": 45,
        "languageCode": "en",
        "sessionStatus": "SCHEDULED",
        "hostName": "Jane",
        "waitlistCount": 3
      }
    }
  ]
}
```
