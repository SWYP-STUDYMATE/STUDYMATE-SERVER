package com.studymate.domain.notification.type;

public enum NotificationStatus {
    PENDING,     // 발송 대기
    SENT,        // 발송됨
    DELIVERED,   // 전달됨
    READ,        // 읽음
    UNREAD,      // 읽지 않음
    FAILED,      // 발송 실패
    CANCELLED,   // 취소됨
    EXPIRED      // 만료됨
}