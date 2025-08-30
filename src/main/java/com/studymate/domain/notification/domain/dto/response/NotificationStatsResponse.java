package com.studymate.domain.notification.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationStatsResponse {
    private Long totalNotifications;
    private Long unreadCount;
    private Long readCount;
    private Long sentCount;
    private Long failedCount;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> categoryBreakdown;
    private Map<String, Long> typeBreakdown;
    private Double averageReadTime; // 평균 읽는 시간 (분)
    private Long todayCount;
    private Long thisWeekCount;
    private Long thisMonthCount;
}