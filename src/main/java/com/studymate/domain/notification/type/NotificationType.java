package com.studymate.domain.notification.type;

public enum NotificationType {
    // 세션 관련
    SESSION_REMINDER,          // 세션 알림
    SESSION_CANCELLED,         // 세션 취소
    SESSION_STARTED,           // 세션 시작
    SESSION_COMPLETED,         // 세션 완료
    
    // 매칭 관련
    MATCH_REQUEST_RECEIVED,    // 매칭 요청 받음
    MATCH_REQUEST_ACCEPTED,    // 매칭 요청 수락됨
    MATCH_REQUEST_REJECTED,    // 매칭 요청 거절됨
    NEW_MATCH_FOUND,          // 새로운 매치 발견
    
    // 채팅 관련
    NEW_MESSAGE,              // 새 메시지
    CHAT_INVITATION,          // 채팅 초대
    
    // 레벨 테스트 관련
    LEVEL_TEST_AVAILABLE,     // 레벨 테스트 가능
    LEVEL_TEST_RESULT,        // 레벨 테스트 결과
    LEVEL_TEST_REMINDER,      // 레벨 테스트 리마인더
    
    // 시스템 관련
    SYSTEM_MAINTENANCE,       // 시스템 점검
    SYSTEM_UPDATE,           // 시스템 업데이트
    ACCOUNT_SECURITY,        // 계정 보안
    PASSWORD_CHANGED,        // 비밀번호 변경
    
    // 학습 관련
    DAILY_STREAK,            // 연속 학습
    ACHIEVEMENT_UNLOCKED,    // 성취 달성
    LEARNING_MILESTONE,      // 학습 마일스톤
    WEEKLY_SUMMARY,          // 주간 요약
    
    // 마케팅 관련
    FEATURE_ANNOUNCEMENT,    // 기능 공지
    PROMOTIONAL_OFFER,       // 프로모션 제안
    NEWSLETTER,              // 뉴스레터
    
    // 기타
    FRIEND_REQUEST,          // 친구 요청
    CUSTOM                   // 사용자 정의
}