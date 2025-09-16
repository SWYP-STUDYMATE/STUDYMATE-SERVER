# STUDYMATE UX 개선 태스크 리스트

## 🎯 Phase 1: Critical UX Issues (우선순위 높음)

### 1. 온보딩 UX 개선

#### 1.1 단계별 저장 시스템 (2-3일)
**태스크:** OnboardController 단계별 저장 API 구현
- `POST /api/v1/onboarding/steps/{stepNumber}/save` - 각 단계별 임시 저장
- `GET /api/v1/onboarding/steps/current` - 현재 진행 단계 조회
- `POST /api/v1/onboarding/steps/{stepNumber}/skip` - 단계 건너뛰기
- Redis 기반 임시 데이터 저장소 구현
- 브라우저 새로고침/재방문 시 이어서 진행 가능

#### 1.2 진행률 표시 UI 백엔드 (1-2일)
**태스크:** 온보드 진행률 추적 API
- `GET /api/v1/onboarding/progress` - 진행률, 예상 남은 시간, 완료 단계
- `GET /api/v1/onboarding/steps/meta` - 각 단계별 메타정보 (필수/선택, 예상시간)
- 단계별 완료 조건 정의 및 검증 로직

#### 1.3 스마트 온보딩 로직 (2-3일)  
**태스크:** 최소 정보 기반 매칭 체험 기능
- 필수 정보 40% 완료 시 "체험 매칭" 가능
- `POST /api/v1/onboarding/trial-matching` - 임시 매칭 체험
- 온보드 미완료 상태에서도 제한적 기능 사용 가능

### 2. 실시간 통신 안정성 개선

#### 2.1 메시지 전송 안정성 (3-4일)
**태스크:** 메시지 전송 상태 추적 시스템
- `POST /api/v1/chat/messages/retry` - 실패 메시지 재전송
- `GET /api/v1/chat/messages/{messageId}/status` - 메시지 전송 상태 조회
- Redis Streams 기반 메시지 큐 시스템 구현
- 메시지 전송 확인 로직 (ACK 시스템)

#### 2.2 WebRTC 연결 최적화 (3-4일)
**태스크:** 화상통화 품질 관리 시스템  
- `POST /api/v1/webrtc/rooms/{roomId}/connection-test` - 네트워크 품질 테스트
- `POST /api/v1/webrtc/rooms/{roomId}/fallback-to-audio` - 음성통화 전환
- `GET /api/v1/webrtc/rooms/{roomId}/quality` - 실시간 품질 모니터링
- WebRTC getStats() API 연동 및 품질 분석

#### 2.3 오프라인 지원 시스템 (2-3일)
**태스크:** 오프라인 메시지 처리
- `POST /api/v1/chat/offline-messages/sync` - 오프라인 메시지 동기화
- `GET /api/v1/users/connection-status` - 사용자 온라인 상태 추적
- FCM 푸시 알림 서비스 연동
- 오프라인 메시지 저장 및 배치 전송 시스템

### 3. 매칭 실패 대안 제시

#### 3.1 스마트 대안 제시 API (2-3일)
**태스크:** 매칭 대안 분석 시스템
- `GET /api/v1/matching/alternatives` - 개인화된 대안 제시
- `GET /api/v1/matching/failure-analysis` - 매칭 실패 원인 분석
- `POST /api/v1/matching/criteria/adjust` - 매칭 조건 동적 조정
- 매칭 성공률 예측 알고리즘 구현

#### 3.2 그룹 세션 시스템 (3-4일)
**태스크:** 1:1 매칭 대안으로 그룹 기능 구현
- `POST /api/v1/group-sessions/join` - 그룹 세션 참여
- `GET /api/v1/group-sessions/available` - 참여 가능한 그룹 조회
- `POST /api/v1/group-sessions/create` - 그룹 세션 생성
- 언어별/레벨별 그룹 채팅방 자동 생성

#### 3.3 AI 연습 파트너 (4-5일)
**태스크:** 매칭 대기 중 AI 챗봇 연습 기능
- `POST /api/v1/ai-practice/start` - AI 연습 세션 시작
- `POST /api/v1/ai-practice/chat` - AI와 대화 연습
- Clova Studio API 확장 활용
- 연습 세션 통계 및 실제 매칭에 반영

## 🚀 Phase 2: Enhanced UX (중기 개선)

### 4. 고급 UX 기능

#### 4.1 대화 시작 도우미 (2일)
**태스크:** 어색함 해소 도구
- `GET /api/v1/chat/conversation-starters` - 레벨별 대화 시작 문장
- `GET /api/v1/chat/topic-suggestions` - 공통 관심사 기반 주제 추천
- 침묵 감지 시 자동 주제 제안

#### 4.2 학습 동기부여 시스템 (3일)
**태스크:** 지속적 참여 유도 기능
- `GET /api/v1/analytics/learning-recommendations` - 개인화 학습 추천
- `POST /api/v1/users/learning-goals` - 학습 목표 설정 및 추적
- 학습 정체기 감지 및 개입 알고리즘

#### 4.3 품질 피드백 루프 (2일)
**태스크:** 지속적 개선을 위한 피드백 시스템
- `POST /api/v1/feedback/session-quality` - 세션 품질 피드백
- `POST /api/v1/feedback/matching-quality` - 매칭 품질 피드백  
- `GET /api/v1/admin/feedback-analytics` - 피드백 분석 대시보드

## 📋 구현 가이드라인

### 개발 원칙
1. **사용자 중심 설계**: 모든 기능은 실제 언어 학습자 관점에서 검증
2. **점진적 개선**: A/B 테스트를 통한 단계별 최적화
3. **성능 우선**: 사용자 경험을 해치지 않는 범위에서 기능 추가
4. **접근성 고려**: 다양한 네트워크 환경 및 디바이스 지원

### 테스트 전략
- **단위 테스트**: 각 새로운 API 엔드포인트
- **통합 테스트**: 온보드 플로우, 메시지 안정성, 매칭 대안
- **E2E 테스트**: 전체 사용자 여정 시나리오
- **부하 테스트**: 실시간 통신 안정성 검증

### 모니터링 지표
- 온보드 단계별 이탈률 추적
- 메시지 전송 성공률 실시간 모니터링  
- 매칭 대안 수용률 분석
- 사용자 만족도 지표 수집

이 태스크들을 통해 STUDYMATE가 **기술적으로 완벽한 플랫폼**에서 **사용자가 사랑하는 플랫폼**으로 진화할 수 있습니다.