# 🎨 프론트엔드 컴포넌트 가이드

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Frontend Development Team  
- **목적**: STUDYMATE 프론트엔드 컴포넌트 구조 및 사용법
- **프론트엔드 위치**: `/Users/minhan/Desktop/public-repo/STYDYMATE-CLIENT`

---

## 🏗️ 프론트엔드 아키텍처 개요

### 기술 스택
- **Framework**: React 19.1.0
- **Build Tool**: Vite 7.0.4
- **Styling**: Tailwind CSS 4.1.11
- **State Management**: Zustand 5.0.6
- **Routing**: React Router 7.6.3
- **HTTP Client**: Axios 1.10.0
- **Testing**: Playwright 1.54.2

### 프로젝트 구조
```
STUDYMATE-CLIENT/
├── src/
│   ├── components/          # 재사용 가능한 컴포넌트
│   │   ├── chat/           # 채팅 관련 컴포넌트
│   │   ├── profile/        # 프로필 관련 컴포넌트
│   │   └── ...             # 기타 공통 컴포넌트
│   ├── pages/              # 페이지 레벨 컴포넌트
│   │   ├── Login/          # 로그인 관련 페이지
│   │   ├── Chat/           # 채팅 페이지
│   │   ├── LevelTest/      # 레벨 테스트 페이지
│   │   └── ...             # 기타 페이지
│   ├── hooks/              # 커스텀 React Hooks
│   ├── store/              # Zustand 상태 관리
│   ├── api/                # API 통신 레이어
│   ├── services/           # 비즈니스 로직
│   └── utils/              # 유틸리티 함수
└── workers/                # Cloudflare Workers (백엔드 보조)
```

---

## 🧩 컴포넌트 분류

### 1. 공통 컴포넌트 (Common Components)

#### CommonButton
모든 버튼의 기본 스타일을 제공하는 공통 버튼 컴포넌트

**파일**: `src/components/CommonButton.jsx`
```jsx
// 사용법 예시
<CommonButton 
  variant="primary"    // primary, secondary, outline
  size="medium"        // small, medium, large
  disabled={false}
  onClick={handleClick}
>
  버튼 텍스트
</CommonButton>
```

**Props:**
- `variant`: 버튼 스타일 변형
- `size`: 버튼 크기
- `disabled`: 비활성화 상태
- `loading`: 로딩 상태 표시

#### OptimizedImage
성능 최적화된 이미지 컴포넌트

**파일**: `src/components/OptimizedImage.jsx`
```jsx
<OptimizedImage
  src="/assets/profile.jpg"
  alt="프로필 이미지"
  width={150}
  height={150}
  lazy={true}
  fallback="/assets/default-profile.png"
/>
```

#### PrograssBar
진행률을 표시하는 프로그레스바 컴포넌트

**파일**: `src/components/PrograssBar.jsx`
```jsx
<PrograssBar
  value={75}          // 0-100
  max={100}
  showLabel={true}
  color="blue"
  className="my-4"
/>
```

### 2. 레이아웃 컴포넌트

#### Header / MainHeader
애플리케이션의 상단 헤더 컴포넌트

**파일**: `src/components/Header.jsx`, `src/components/MainHeader.jsx`
```jsx
<MainHeader
  user={currentUser}
  showNotifications={true}
  onMenuClick={handleMenuClick}
/>
```

#### DashboardLayout
대시보드 페이지의 레이아웃 컴포넌트

**파일**: `src/components/DashboardLayout.jsx`
```jsx
<DashboardLayout
  sidebar={<Sidebar />}
  header={<Header />}
>
  {children}
</DashboardLayout>
```

### 3. 채팅 관련 컴포넌트

#### ChatContainer
채팅 기능의 최상위 컨테이너 컴포넌트

**파일**: `src/components/chat/ChatContainer.jsx`
```jsx
<ChatContainer
  roomId="room-uuid-123"
  userId="user-uuid-456"
  onMessageSend={handleMessageSend}
/>
```

#### ChatMessageList
채팅 메시지 목록을 표시하는 컴포넌트

**파일**: `src/components/chat/ChatMessageList.jsx`
```jsx
<ChatMessageList
  messages={messages}
  currentUserId="user-uuid-456"
  onMessageClick={handleMessageClick}
  loading={isLoading}
/>
```

#### ChatInputArea
메시지 입력 영역 컴포넌트

**파일**: `src/components/chat/ChatInputArea.jsx`
```jsx
<ChatInputArea
  onSendMessage={handleSendMessage}
  onFileUpload={handleFileUpload}
  onVoiceRecord={handleVoiceRecord}
  placeholder="메시지를 입력하세요..."
/>
```

#### VoiceRecorder
음성 메시지 녹음 컴포넌트

**파일**: `src/components/chat/VoiceRecorder.jsx`
```jsx
<VoiceRecorder
  onRecordingComplete={handleRecordingComplete}
  maxDuration={60} // 최대 60초
  enabled={true}
/>
```

### 4. 세션 관련 컴포넌트

#### VideoControls
화상 통화 제어 컴포넌트

**파일**: `src/components/VideoControls.jsx`
```jsx
<VideoControls
  localStream={localStream}
  remoteStream={remoteStream}
  isAudioEnabled={isAudioEnabled}
  isVideoEnabled={isVideoEnabled}
  onToggleAudio={handleToggleAudio}
  onToggleVideo={handleToggleVideo}
  onEndCall={handleEndCall}
/>
```

#### AudioRecorder
오디오 녹음 컴포넌트

**파일**: `src/components/AudioRecorder.jsx`
```jsx
<AudioRecorder
  onRecordingStart={handleRecordingStart}
  onRecordingStop={handleRecordingStop}
  onRecordingComplete={handleRecordingComplete}
  maxDuration={180} // 3분
/>
```

### 5. 레벨 테스트 컴포넌트

#### CountdownTimer
카운트다운 타이머 컴포넌트

**파일**: `src/components/CountdownTimer.jsx`
```jsx
<CountdownTimer
  duration={60} // 60초
  onComplete={handleTimeUp}
  onTick={handleTick}
  format="mm:ss"
/>
```

#### LiveTranscription
실시간 음성 인식 표시 컴포넌트

**파일**: `src/components/LiveTranscription.jsx`
```jsx
<LiveTranscription
  isActive={isRecording}
  language="en"
  onTranscriptionUpdate={handleTranscriptionUpdate}
/>
```

---

## 🎨 스타일 가이드

### 컬러 시스템
```css
/* Primary Colors */
:root {
  --color-primary-50: #eff6ff;
  --color-primary-500: #3b82f6;
  --color-primary-600: #2563eb;
  --color-primary-700: #1d4ed8;
  
  /* Secondary Colors */
  --color-secondary-500: #6b7280;
  --color-secondary-600: #4b5563;
  
  /* Success/Error Colors */
  --color-success: #10b981;
  --color-error: #ef4444;
  --color-warning: #f59e0b;
}
```

### 타이포그래피
```css
/* Font Family */
body {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Font Sizes */
.text-xs { font-size: 0.75rem; }
.text-sm { font-size: 0.875rem; }
.text-base { font-size: 1rem; }
.text-lg { font-size: 1.125rem; }
.text-xl { font-size: 1.25rem; }
.text-2xl { font-size: 1.5rem; }
```

### 스페이싱 시스템
```css
/* Tailwind 기본 스페이싱 사용 */
.p-4 { padding: 1rem; }      /* 16px */
.m-6 { margin: 1.5rem; }     /* 24px */
.gap-8 { gap: 2rem; }        /* 32px */
```

### 반응형 디자인
```css
/* Breakpoints */
/* sm: 640px */
/* md: 768px */
/* lg: 1024px */
/* xl: 1280px */

.container {
  @apply mx-auto px-4;
  @apply sm:px-6 md:px-8;
  @apply max-w-7xl;
}
```

---

## 🔗 상태 관리 (Zustand Stores)

### 1. 사용자 프로필 스토어
**파일**: `src/store/profileStore.js`
```javascript
import { create } from 'zustand';

const useProfileStore = create((set, get) => ({
  // 상태
  profile: null,
  loading: false,
  error: null,
  
  // 액션
  setProfile: (profile) => set({ profile }),
  
  updateProfile: async (updates) => {
    set({ loading: true, error: null });
    try {
      const response = await api.put('/api/v1/user/profile', updates);
      set({ profile: response.data, loading: false });
    } catch (error) {
      set({ error: error.message, loading: false });
    }
  },
  
  clearProfile: () => set({ profile: null, error: null })
}));
```

### 2. 언어 정보 스토어
**파일**: `src/store/langInfoStore.js`
```javascript
const useLangInfoStore = create((set) => ({
  selectedLanguages: [],
  nativeLanguage: null,
  learningLevel: 'A1',
  
  setSelectedLanguages: (languages) => set({ selectedLanguages: languages }),
  setNativeLanguage: (language) => set({ nativeLanguage: language }),
  setLearningLevel: (level) => set({ learningLevel: level })
}));
```

### 3. 레벨 테스트 스토어
**파일**: `src/store/levelTestStore.js`
```javascript
const useLevelTestStore = create((set, get) => ({
  currentTest: null,
  testResults: null,
  isRecording: false,
  currentQuestionIndex: 0,
  
  startTest: (testId) => set({ 
    currentTest: testId, 
    currentQuestionIndex: 0,
    testResults: null 
  }),
  
  nextQuestion: () => set((state) => ({
    currentQuestionIndex: state.currentQuestionIndex + 1
  })),
  
  setTestResults: (results) => set({ testResults: results }),
  
  resetTest: () => set({
    currentTest: null,
    testResults: null,
    isRecording: false,
    currentQuestionIndex: 0
  })
}));
```

---

## 🎣 커스텀 Hooks

### 1. useImageUpload
이미지 업로드 기능을 제공하는 커스텀 훅

**파일**: `src/hooks/useImageUpload.js`
```javascript
import { useState } from 'react';
import api from '../api';

export const useImageUpload = () => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  
  const uploadImage = async (file) => {
    setUploading(true);
    setError(null);
    
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await api.post('/api/v1/user/profile-image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      
      setUploading(false);
      return response.data.imageUrl;
    } catch (err) {
      setError(err.message);
      setUploading(false);
      throw err;
    }
  };
  
  return { uploadImage, uploading, error };
};
```

### 2. useWebRTC
WebRTC 연결을 관리하는 커스텀 훅

**파일**: `src/hooks/useWebRTC.js`
```javascript
import { useState, useEffect, useRef } from 'react';

export const useWebRTC = () => {
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const peerConnection = useRef(null);
  
  const startCall = async () => {
    try {
      // 미디어 스트림 획득
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      });
      setLocalStream(stream);
      
      // WebRTC 연결 설정
      peerConnection.current = new RTCPeerConnection({
        iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
      });
      
      // 이벤트 핸들러 설정
      peerConnection.current.onicecandidate = handleIceCandidate;
      peerConnection.current.ontrack = handleRemoteStream;
      
      // 로컬 스트림 추가
      stream.getTracks().forEach(track => {
        peerConnection.current.addTrack(track, stream);
      });
      
    } catch (error) {
      console.error('WebRTC setup failed:', error);
    }
  };
  
  const endCall = () => {
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop());
      setLocalStream(null);
    }
    if (peerConnection.current) {
      peerConnection.current.close();
      peerConnection.current = null;
    }
    setRemoteStream(null);
    setIsConnected(false);
  };
  
  return {
    localStream,
    remoteStream,
    isConnected,
    startCall,
    endCall
  };
};
```

### 3. useLLM
LLM API 호출을 위한 커스텀 훅

**파일**: `src/hooks/useLLM.js`
```javascript
import { useState } from 'react';

export const useLLM = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const correctText = async (text, language = 'en') => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/api/v1/clova/correct', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        },
        body: JSON.stringify({ text, language })
      });
      
      const result = await response.json();
      setLoading(false);
      return result.data;
    } catch (err) {
      setError(err.message);
      setLoading(false);
      throw err;
    }
  };
  
  return { correctText, loading, error };
};
```

---

## 🎪 페이지 컴포넌트 구조

### 1. 로그인 페이지
**파일**: `src/pages/Login/Login.jsx`
```jsx
const Login = () => {
  const navigate = useNavigate();
  
  const handleNaverLogin = () => {
    // Naver OAuth 로그인 처리
    window.location.href = naverAuthUrl;
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold">STUDYMATE</h1>
          <p className="mt-2 text-gray-600">언어교환 학습 플랫폼</p>
        </div>
        
        <div className="space-y-4">
          <CommonButton
            variant="primary"
            size="large"
            onClick={handleNaverLogin}
            className="w-full"
          >
            네이버로 시작하기
          </CommonButton>
        </div>
      </div>
    </div>
  );
};
```

### 2. 채팅 페이지
**파일**: `src/pages/Chat/ChatPage.jsx`
```jsx
const ChatPage = () => {
  const { roomId } = useParams();
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  
  return (
    <div className="flex h-screen">
      <div className="w-1/4 bg-gray-100">
        <ChatRoomList />
      </div>
      
      <div className="flex-1 flex flex-col">
        <ChatHeader roomId={roomId} />
        
        <div className="flex-1 overflow-y-auto">
          <ChatMessageList 
            messages={messages}
            currentUserId={currentUser.id}
          />
        </div>
        
        <ChatInputArea
          onSendMessage={handleSendMessage}
          value={newMessage}
          onChange={setNewMessage}
        />
      </div>
    </div>
  );
};
```

### 3. 레벨 테스트 페이지
**파일**: `src/pages/LevelTest/LevelTestStart.jsx`
```jsx
const LevelTestStart = () => {
  const navigate = useNavigate();
  const { startTest } = useLevelTestStore();
  
  const handleStartTest = async () => {
    try {
      const response = await api.post('/api/v1/level-test/start');
      const { testId } = response.data;
      
      startTest(testId);
      navigate('/level-test/recording');
    } catch (error) {
      console.error('Test start failed:', error);
    }
  };
  
  return (
    <div className="max-w-2xl mx-auto p-8">
      <div className="text-center space-y-6">
        <h1 className="text-4xl font-bold">AI 레벨 테스트</h1>
        <p className="text-lg text-gray-600">
          음성 인식 기반으로 영어 실력을 정확하게 진단합니다
        </p>
        
        <div className="bg-blue-50 p-6 rounded-lg">
          <h3 className="font-semibold mb-4">테스트 안내</h3>
          <ul className="text-left space-y-2">
            <li>• 총 5개의 질문에 답변해주세요</li>
            <li>• 각 질문당 최대 60초 답변 시간</li>
            <li>• 조용한 환경에서 진행해주세요</li>
          </ul>
        </div>
        
        <CommonButton
          variant="primary"
          size="large"
          onClick={handleStartTest}
        >
          테스트 시작하기
        </CommonButton>
      </div>
    </div>
  );
};
```

---

## 🔌 API 통신 레이어

### API 클라이언트 설정
**파일**: `src/api/index.js`
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
});

// 요청 인터셉터
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // 토큰 만료 시 자동 갱신 로직
    if (error.response?.status === 401) {
      // 토큰 갱신 로직
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 🧪 테스트 가이드

### E2E 테스트 (Playwright)
**파일**: `e2e/auth.spec.js`
```javascript
import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should login with Naver OAuth', async ({ page }) => {
    await page.goto('/');
    
    await page.click('[data-testid="naver-login-button"]');
    
    // OAuth 플로우 테스트
    await expect(page).toHaveURL(/nid\.naver\.com/);
    
    // 로그인 후 리다이렉트 확인
    await expect(page).toHaveURL('/main');
  });
  
  test('should navigate through onboarding process', async ({ page }) => {
    await page.goto('/onboarding/language');
    
    // 언어 선택
    await page.click('[data-testid="language-english"]');
    await page.click('[data-testid="level-intermediate"]');
    await page.click('[data-testid="next-button"]');
    
    await expect(page).toHaveURL('/onboarding/interests');
  });
});
```

---

## 📱 모바일 대응

### 반응형 디자인 원칙
```jsx
// 모바일 우선 설계
<div className="
  w-full p-4
  sm:max-w-md sm:p-6
  md:max-w-lg md:p-8
  lg:max-w-xl lg:p-10
">
  {/* 컨텐츠 */}
</div>

// 모바일에서 숨김/표시
<div className="hidden md:block">
  {/* 데스크톱에서만 표시 */}
</div>

<div className="block md:hidden">
  {/* 모바일에서만 표시 */}
</div>
```

### 터치 친화적 UI
```css
/* 최소 터치 타겟 크기 */
.touch-target {
  min-height: 44px;
  min-width: 44px;
}

/* 터치 피드백 */
.button:active {
  transform: scale(0.98);
  transition: transform 0.1s;
}
```

---

## 📚 관련 문서

- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [API 레퍼런스](../04-api/api-reference.md)
- [백엔드 서비스](../07-backend/services-overview.md)
- [스타일 가이드](./style-guide.md)
- [프론트엔드 상세 문서](../../STYDYMATE-CLIENT/docs/ARCHITECTURE.md)