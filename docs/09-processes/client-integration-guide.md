# Client Integration Guide

## 1. 개요

STUDYMATE 백엔드 API와 프론트엔드 클라이언트 간의 통합 가이드입니다. 
React 기반 클라이언트에서 Spring Boot API 서버와 안전하고 효율적으로 통신하는 방법을 다룹니다.

## 2. 인증 시스템 연동

### 2.1 OAuth 로그인 플로우

```typescript
// 네이버 OAuth 로그인 시작
const initiateNaverLogin = () => {
  const clientId = process.env.REACT_APP_NAVER_CLIENT_ID;
  const redirectUri = encodeURIComponent(`${window.location.origin}/callback/naver`);
  const state = generateRandomString(32); // CSRF 방지
  
  localStorage.setItem('oauth_state', state);
  
  window.location.href = `https://nid.naver.com/oauth2.0/authorize?` +
    `response_type=code&` +
    `client_id=${clientId}&` +
    `redirect_uri=${redirectUri}&` +
    `state=${state}`;
};

// 콜백 처리
const handleOAuthCallback = async (code: string, state: string) => {
  // State 검증
  if (state !== localStorage.getItem('oauth_state')) {
    throw new Error('Invalid state parameter');
  }
  
  // 백엔드 콜백 엔드포인트 호출
  const response = await fetch('/api/v1/auth/naver/callback', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, state })
  });
  
  const { data } = await response.json();
  
  // 토큰 저장
  localStorage.setItem('access_token', data.accessToken);
  localStorage.setItem('refresh_token', data.refreshToken);
  
  return data;
};
```

### 2.2 JWT 토큰 관리

```typescript
// Axios 인터셉터 설정
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
  timeout: 10000,
});

// 요청 인터셉터 - 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터 - 토큰 갱신 처리
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refresh_token');
        const response = await axios.post('/api/v1/auth/refresh', {
          refreshToken
        });
        
        const { accessToken } = response.data.data;
        localStorage.setItem('access_token', accessToken);
        
        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 토큰 갱신 실패 시 로그아웃
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

## 3. WebSocket 실시간 채팅 연동

### 3.1 STOMP 클라이언트 설정

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class ChatService {
  private stompClient: Client | null = null;
  private connected = false;

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('access_token');
      
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => console.log('STOMP Debug:', str),
        onConnect: () => {
          this.connected = true;
          console.log('WebSocket Connected');
          resolve();
        },
        onDisconnect: () => {
          this.connected = false;
          console.log('WebSocket Disconnected');
        },
        onStompError: (frame) => {
          console.error('STOMP Error:', frame);
          reject(new Error(frame.headers['message']));
        }
      });

      this.stompClient.activate();
    });
  }

  // 채팅방 구독
  subscribeToRoom(roomId: string, callback: (message: any) => void) {
    if (!this.stompClient || !this.connected) {
      throw new Error('WebSocket not connected');
    }

    return this.stompClient.subscribe(
      `/topic/chat/${roomId}`,
      (message) => {
        const parsedMessage = JSON.parse(message.body);
        callback(parsedMessage);
      }
    );
  }

  // 메시지 전송
  sendMessage(roomId: string, content: string, messageType: 'TEXT' | 'IMAGE' = 'TEXT') {
    if (!this.stompClient || !this.connected) {
      throw new Error('WebSocket not connected');
    }

    this.stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        roomId,
        content,
        messageType
      })
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}

export const chatService = new ChatService();
```

### 3.2 채팅 컴포넌트 통합

```typescript
import React, { useState, useEffect, useRef } from 'react';
import { chatService } from '../services/chatService';

interface ChatMessage {
  id: string;
  senderId: string;
  senderName: string;
  content: string;
  messageType: 'TEXT' | 'IMAGE';
  timestamp: string;
}

const ChatRoom: React.FC<{ roomId: string }> = ({ roomId }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const initChat = async () => {
      try {
        // WebSocket 연결
        await chatService.connect();
        setConnected(true);

        // 채팅방 구독
        const subscription = chatService.subscribeToRoom(roomId, (message) => {
          setMessages(prev => [...prev, message]);
        });

        // 기존 메시지 로드
        const response = await apiClient.get(`/api/v1/chat/rooms/${roomId}/messages`);
        setMessages(response.data.data.content);

        return () => {
          subscription.unsubscribe();
          chatService.disconnect();
        };
      } catch (error) {
        console.error('Chat initialization failed:', error);
      }
    };

    initChat();
  }, [roomId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = () => {
    if (!newMessage.trim() || !connected) return;

    chatService.sendMessage(roomId, newMessage);
    setNewMessage('');
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((message) => (
          <div key={message.id} className="message">
            <strong>{message.senderName}:</strong> {message.content}
            <small>{new Date(message.timestamp).toLocaleTimeString()}</small>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      
      <div className="input-area">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          disabled={!connected}
          placeholder="메시지를 입력하세요..."
        />
        <button onClick={handleSendMessage} disabled={!connected}>
          전송
        </button>
      </div>
      
      {!connected && <div className="status">연결 중...</div>}
    </div>
  );
};
```

## 4. Analytics & Achievement 시스템 연동

### 4.1 사용자 활동 추적

```typescript
// Analytics 서비스
class AnalyticsService {
  // 활동 기록
  async recordActivity(activityType: string, metadata?: Record<string, any>) {
    try {
      await apiClient.post('/api/v1/analytics/activities', {
        activityType,
        metadata
      });
    } catch (error) {
      console.error('Activity recording failed:', error);
    }
  }

  // 사용자 통계 조회
  async getUserStats(period: 'DAILY' | 'WEEKLY' | 'MONTHLY' = 'WEEKLY') {
    const response = await apiClient.get('/api/v1/analytics/users/stats', {
      params: { period }
    });
    return response.data.data;
  }

  // 학습 세션 기록
  async recordSession(partnerId: string, duration: number, sessionType: 'CHAT' | 'VIDEO' | 'VOICE') {
    await this.recordActivity('LEARNING_SESSION', {
      partnerId,
      duration,
      sessionType,
      timestamp: new Date().toISOString()
    });
  }
}

export const analyticsService = new AnalyticsService();

// 사용 예시
const ChatComponent = () => {
  const handleMessageSent = () => {
    // 메시지 전송 후 활동 기록
    analyticsService.recordActivity('MESSAGE_SENT', {
      roomId: currentRoomId,
      messageLength: message.length
    });
  };

  const handleSessionStart = (partnerId: string) => {
    // 학습 세션 시작 기록
    analyticsService.recordActivity('SESSION_START', {
      partnerId,
      sessionType: 'CHAT'
    });
  };
};
```

### 4.2 성취 시스템 UI 연동

```typescript
// Achievement 서비스
class AchievementService {
  // 성취도 진행률 조회
  async getProgress() {
    const response = await apiClient.get('/api/v1/achievements/progress');
    return response.data.data;
  }

  // 보상 수령
  async claimReward(achievementId: string) {
    const response = await apiClient.post(`/api/v1/achievements/${achievementId}/claim`);
    return response.data.data;
  }

  // 성취 통계
  async getStatistics() {
    const response = await apiClient.get('/api/v1/achievements/statistics');
    return response.data.data;
  }
}

// Achievement 컴포넌트
const AchievementPanel: React.FC = () => {
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadAchievements = async () => {
      try {
        const progress = await achievementService.getProgress();
        setAchievements(progress);
      } catch (error) {
        console.error('Achievement loading failed:', error);
      } finally {
        setLoading(false);
      }
    };

    loadAchievements();
  }, []);

  const handleClaimReward = async (achievementId: string) => {
    try {
      const reward = await achievementService.claimReward(achievementId);
      // UI 업데이트 및 성공 알림
      toast.success(`보상 획득: ${reward.description}`);
      // 성취도 목록 새로고침
    } catch (error) {
      toast.error('보상 수령에 실패했습니다.');
    }
  };

  return (
    <div className="achievement-panel">
      {achievements.map((achievement) => (
        <div key={achievement.id} className="achievement-card">
          <h3>{achievement.title}</h3>
          <p>{achievement.description}</p>
          <div className="progress-bar">
            <div 
              className="progress-fill"
              style={{ width: `${achievement.progressPercentage}%` }}
            />
          </div>
          <span>{achievement.currentValue} / {achievement.targetValue}</span>
          
          {achievement.isCompleted && !achievement.isClaimed && (
            <button onClick={() => handleClaimReward(achievement.id)}>
              보상 수령
            </button>
          )}
        </div>
      ))}
    </div>
  );
};
```

## 5. 매칭 시스템 연동

### 5.1 파트너 검색 및 매칭

```typescript
interface PartnerSearchParams {
  learningLanguage?: string;
  nativeLanguage?: string;
  minAge?: number;
  maxAge?: number;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  personalityType?: string;
  page?: number;
  size?: number;
}

class MatchingService {
  // 기본 파트너 검색
  async searchPartners(params: PartnerSearchParams) {
    const response = await apiClient.get('/api/v1/matching/search', { params });
    return response.data.data;
  }

  // AI 기반 스마트 매칭
  async getSmartRecommendations(limit: number = 10) {
    const response = await apiClient.get('/api/v1/matching/smart-recommendations', {
      params: { limit }
    });
    return response.data.data;
  }

  // 매칭 요청 전송
  async sendMatchingRequest(targetUserId: string, message?: string) {
    const response = await apiClient.post('/api/v1/matching/request', {
      targetUserId,
      message
    });
    return response.data.data;
  }

  // 매칭 요청 응답
  async respondToRequest(requestId: string, accepted: boolean) {
    const response = await apiClient.post(`/api/v1/matching/requests/${requestId}/respond`, {
      accepted
    });
    return response.data.data;
  }

  // 매칭 큐 참여
  async joinMatchingQueue(preferences: any) {
    const response = await apiClient.post('/api/v1/matching/queue/join', preferences);
    return response.data.data;
  }
}

export const matchingService = new MatchingService();
```

### 5.2 매칭 UI 컴포넌트

```typescript
const PartnerSearch: React.FC = () => {
  const [partners, setPartners] = useState([]);
  const [smartRecommendations, setSmartRecommendations] = useState([]);
  const [searchParams, setSearchParams] = useState<PartnerSearchParams>({});
  const [loading, setLoading] = useState(false);

  // 스마트 추천 로드
  useEffect(() => {
    const loadRecommendations = async () => {
      try {
        const recommendations = await matchingService.getSmartRecommendations();
        setSmartRecommendations(recommendations);
      } catch (error) {
        console.error('Recommendations loading failed:', error);
      }
    };

    loadRecommendations();
  }, []);

  const handleSearch = async () => {
    setLoading(true);
    try {
      const results = await matchingService.searchPartners(searchParams);
      setPartners(results.content);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMatchingRequest = async (partnerId: string) => {
    try {
      await matchingService.sendMatchingRequest(partnerId, '함께 언어교환 하고 싶습니다!');
      toast.success('매칭 요청을 전송했습니다.');
    } catch (error) {
      toast.error('매칭 요청 전송에 실패했습니다.');
    }
  };

  return (
    <div className="partner-search">
      {/* 검색 필터 */}
      <div className="search-filters">
        <select 
          value={searchParams.learningLanguage || ''}
          onChange={(e) => setSearchParams({...searchParams, learningLanguage: e.target.value})}
        >
          <option value="">학습 언어 선택</option>
          <option value="EN">English</option>
          <option value="KO">한국어</option>
        </select>
        
        <button onClick={handleSearch} disabled={loading}>
          {loading ? '검색 중...' : '검색'}
        </button>
      </div>

      {/* AI 추천 섹션 */}
      <div className="recommendations">
        <h3>AI 추천 파트너</h3>
        <div className="partner-grid">
          {smartRecommendations.map((partner) => (
            <PartnerCard 
              key={partner.id}
              partner={partner}
              onMatchingRequest={() => handleMatchingRequest(partner.id)}
              isRecommended={true}
            />
          ))}
        </div>
      </div>

      {/* 검색 결과 */}
      <div className="search-results">
        <h3>검색 결과</h3>
        <div className="partner-grid">
          {partners.map((partner) => (
            <PartnerCard 
              key={partner.id}
              partner={partner}
              onMatchingRequest={() => handleMatchingRequest(partner.id)}
            />
          ))}
        </div>
      </div>
    </div>
  );
};
```

## 6. 에러 처리 및 사용자 경험

### 6.1 통합 에러 처리

```typescript
// API 응답 타입 정의
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    details?: any;
  };
}

// 에러 처리 유틸리티
class ErrorHandler {
  static handle(error: any) {
    if (error.response?.data?.error) {
      const { code, message } = error.response.data.error;
      
      switch (code) {
        case 'UNAUTHORIZED':
          this.handleUnauthorized();
          break;
        case 'FORBIDDEN':
          toast.error('접근 권한이 없습니다.');
          break;
        case 'USER_NOT_FOUND':
          toast.error('사용자를 찾을 수 없습니다.');
          break;
        case 'INVALID_INPUT':
          toast.error('입력값이 올바르지 않습니다.');
          break;
        default:
          toast.error(message || '오류가 발생했습니다.');
      }
    } else if (error.code === 'NETWORK_ERROR') {
      toast.error('네트워크 연결을 확인해주세요.');
    } else {
      toast.error('예상치 못한 오류가 발생했습니다.');
    }
  }

  private static handleUnauthorized() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    window.location.href = '/login';
  }
}

// React Hook으로 API 호출 래핑
const useApi = <T>(apiCall: () => Promise<T>) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await apiCall();
      setData(result);
    } catch (err) {
      ErrorHandler.handle(err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return { data, loading, error, execute };
};
```

### 6.2 로딩 상태 및 오프라인 처리

```typescript
// 네트워크 상태 훅
const useNetworkStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
};

// 오프라인 배너 컴포넌트
const OfflineBanner: React.FC = () => {
  const isOnline = useNetworkStatus();

  if (isOnline) return null;

  return (
    <div className="offline-banner">
      네트워크 연결이 끊어졌습니다. 연결을 확인해주세요.
    </div>
  );
};
```

## 7. 성능 최적화

### 7.1 API 응답 캐싱

```typescript
// React Query를 활용한 데이터 페칭 및 캐싱
import { useQuery, useMutation, useQueryClient } from 'react-query';

// 사용자 프로필 캐싱
const useUserProfile = () => {
  return useQuery(
    'userProfile',
    () => apiClient.get('/api/v1/user/profile').then(res => res.data.data),
    {
      staleTime: 5 * 60 * 1000, // 5분간 캐시 유지
      cacheTime: 10 * 60 * 1000, // 10분간 백그라운드 캐시 유지
    }
  );
};

// 파트너 검색 결과 캐싱
const usePartnerSearch = (params: PartnerSearchParams) => {
  return useQuery(
    ['partnerSearch', params],
    () => matchingService.searchPartners(params),
    {
      staleTime: 2 * 60 * 1000, // 2분간 캐시 유지
      enabled: !!params.learningLanguage, // 필수 파라미터 있을 때만 실행
    }
  );
};

// 매칭 요청 뮤테이션
const useMatchingRequest = () => {
  const queryClient = useQueryClient();
  
  return useMutation(
    (data: { targetUserId: string; message?: string }) =>
      matchingService.sendMatchingRequest(data.targetUserId, data.message),
    {
      onSuccess: () => {
        // 매칭 요청 목록 캐시 무효화
        queryClient.invalidateQueries('matchingRequests');
        toast.success('매칭 요청을 전송했습니다.');
      },
      onError: (error) => {
        ErrorHandler.handle(error);
      },
    }
  );
};
```

### 7.2 이미지 최적화 및 지연 로딩

```typescript
// 프로필 이미지 컴포넌트
const ProfileImage: React.FC<{
  src: string;
  alt: string;
  size?: 'sm' | 'md' | 'lg';
}> = ({ src, alt, size = 'md' }) => {
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState(false);

  const sizeClasses = {
    sm: 'w-8 h-8',
    md: 'w-12 h-12',
    lg: 'w-20 h-20'
  };

  const handleLoad = () => setLoaded(true);
  const handleError = () => setError(true);

  if (error) {
    return (
      <div className={`${sizeClasses[size]} bg-gray-300 rounded-full flex items-center justify-center`}>
        <span className="text-gray-500 text-xs">?</span>
      </div>
    );
  }

  return (
    <div className={`${sizeClasses[size]} relative overflow-hidden rounded-full`}>
      {!loaded && (
        <div className="absolute inset-0 bg-gray-200 animate-pulse" />
      )}
      <img
        src={src}
        alt={alt}
        className={`w-full h-full object-cover transition-opacity duration-300 ${
          loaded ? 'opacity-100' : 'opacity-0'
        }`}
        onLoad={handleLoad}
        onError={handleError}
        loading="lazy"
      />
    </div>
  );
};
```

## 8. 보안 고려사항

### 8.1 XSS 방지

```typescript
// 메시지 내용 sanitize
import DOMPurify from 'dompurify';

const sanitizeMessage = (message: string): string => {
  return DOMPurify.sanitize(message, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong'],
    ALLOWED_ATTR: []
  });
};

// 안전한 메시지 렌더링
const MessageContent: React.FC<{ content: string }> = ({ content }) => {
  const sanitizedContent = sanitizeMessage(content);
  
  return (
    <div 
      dangerouslySetInnerHTML={{ __html: sanitizedContent }}
      className="message-content"
    />
  );
};
```

### 8.2 CSRF 방지 및 요청 검증

```typescript
// API 요청에 CSRF 토큰 포함
const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');

if (csrfToken) {
  apiClient.defaults.headers.common['X-CSRF-TOKEN'] = csrfToken;
}

// 민감한 작업에 추가 확인
const ConfirmedApiCall: React.FC<{
  children: React.ReactNode;
  onConfirm: () => Promise<void>;
  message: string;
}> = ({ children, onConfirm, message }) => {
  const [loading, setLoading] = useState(false);

  const handleClick = async () => {
    if (!confirm(message)) return;
    
    setLoading(true);
    try {
      await onConfirm();
    } finally {
      setLoading(false);
    }
  };

  return (
    <button onClick={handleClick} disabled={loading}>
      {loading ? '처리 중...' : children}
    </button>
  );
};
```

## 9. 테스트 가이드

### 9.1 API 통합 테스트

```typescript
// Jest + React Testing Library
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { rest } from 'msw';
import { setupServer } from 'msw/node';

// Mock 서버 설정
const server = setupServer(
  rest.post('/api/v1/auth/naver/callback', (req, res, ctx) => {
    return res(ctx.json({
      success: true,
      data: {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token'
      }
    }));
  }),

  rest.get('/api/v1/matching/search', (req, res, ctx) => {
    return res(ctx.json({
      success: true,
      data: {
        content: [
          {
            id: '1',
            name: 'Test Partner',
            learningLanguage: 'EN',
            nativeLanguage: 'KO'
          }
        ]
      }
    }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// 컴포넌트 테스트
test('파트너 검색이 정상적으로 작동한다', async () => {
  render(<PartnerSearch />);
  
  const searchButton = screen.getByText('검색');
  await userEvent.click(searchButton);
  
  await waitFor(() => {
    expect(screen.getByText('Test Partner')).toBeInTheDocument();
  });
});
```

## 10. 배포 및 환경 설정

### 10.1 환경 변수 설정

```bash
# .env.production
REACT_APP_API_URL=https://api.languagemate.kr
REACT_APP_WS_URL=wss://api.languagemate.kr/ws
REACT_APP_NAVER_CLIENT_ID=your_naver_client_id
REACT_APP_GOOGLE_CLIENT_ID=your_google_client_id
REACT_APP_SENTRY_DSN=your_sentry_dsn

# .env.development
REACT_APP_API_URL=http://localhost:8080
REACT_APP_WS_URL=ws://localhost:8080/ws
REACT_APP_NAVER_CLIENT_ID=dev_naver_client_id
```

### 10.2 빌드 최적화

```javascript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          stomp: ['@stomp/stompjs', 'sockjs-client'],
          ui: ['@radix-ui/react-dialog', '@radix-ui/react-toast']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
});
```

이 가이드는 STUDYMATE 백엔드 API와의 효율적인 통합을 위한 모든 필수 사항들을 다루고 있습니다. 각 섹션의 코드 예제를 참조하여 안전하고 성능이 우수한 클라이언트 애플리케이션을 개발하시기 바랍니다.