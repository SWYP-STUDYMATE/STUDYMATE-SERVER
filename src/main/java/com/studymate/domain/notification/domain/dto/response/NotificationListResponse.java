package com.studymate.domain.notification.domain.dto.response;

import com.studymate.domain.notification.type.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationItem> notifications;
    private long unreadCount;
    private Pagination pagination;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItem {
        private Long id;
        private String type;
        private String category;
        private String title;
        private String message;
        private String content;
        private boolean isRead;
        private NotificationStatus status;
        private Integer priority;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;
        private LocalDateTime scheduledAt;
        private LocalDateTime expiresAt;
        private String clickUrl;
        private Map<String, Object> data;
        private String imageUrl;
        private String iconUrl;
        private boolean highPriority;
        private boolean expired;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
    }
}
