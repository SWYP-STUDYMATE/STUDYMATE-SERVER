package com.studymate.domain.matching.entity;

/**
 * 매칭 요청 상태를 나타내는 ENUM
 */
public enum MatchingStatus {
    PENDING,    // 대기중
    ACCEPTED,   // 수락됨
    REJECTED,   // 거절됨
    CANCELLED,  // 취소됨
    EXPIRED     // 만료됨
}