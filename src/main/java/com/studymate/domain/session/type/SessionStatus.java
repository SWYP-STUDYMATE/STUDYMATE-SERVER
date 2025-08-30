package com.studymate.domain.session.type;

public enum SessionStatus {
    SCHEDULED,    // 예약됨
    IN_PROGRESS,  // 진행 중
    COMPLETED,    // 완료
    CANCELLED,    // 취소됨
    NO_SHOW       // 불참
}