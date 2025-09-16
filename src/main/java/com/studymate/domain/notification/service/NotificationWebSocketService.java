package com.studymate.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 개인 알림 전송
     */
    public void sendPersonalNotification(UUID userId, String title, String message, Map<String, Object> data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PERSONAL");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("data", data);

        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent personal notification to user: {}", userId);
    }

    /**
     * 매칭 알림 전송
     */
    public void sendMatchingNotification(UUID userId, UUID matchedUserId, String matchedUserName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MATCHING");
        notification.put("title", "새로운 매칭!");
        notification.put("message", matchedUserName + "님과 매칭되었습니다.");
        notification.put("matchedUserId", matchedUserId);
        notification.put("matchedUserName", matchedUserName);
        notification.put("timestamp", System.currentTimeMillis());

        String destination = "/user/" + userId + "/queue/matching-notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent matching notification to user: {}", userId);
    }

    /**
     * 세션 알림 전송
     */
    public void sendSessionNotification(UUID userId, String sessionType, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SESSION");
        notification.put("sessionType", sessionType);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        String destination = "/user/" + userId + "/queue/session-notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent session notification to user: {}", userId);
    }

    /**
     * 채팅 알림 전송 (채팅방 외부에서)
     */
    public void sendChatNotification(UUID userId, UUID senderId, String senderName, String preview) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHAT");
        notification.put("title", "새 메시지");
        notification.put("message", senderName + ": " + preview);
        notification.put("senderId", senderId);
        notification.put("senderName", senderName);
        notification.put("preview", preview);
        notification.put("timestamp", System.currentTimeMillis());

        String destination = "/user/" + userId + "/queue/chat-notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent chat notification to user: {}", userId);
    }

    /**
     * 시스템 알림 브로드캐스트
     */
    public void broadcastSystemNotification(String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/sub/system-notifications", notification);
        log.info("Broadcast system notification: {}", title);
    }

    /**
     * 긴급 알림 브로드캐스트
     */
    public void broadcastUrgentNotification(String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "URGENT");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("requireInteraction", true);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/sub/urgent-notifications", notification);
        log.info("Broadcast urgent notification: {}", title);
    }

    /**
     * 알림 상태 업데이트 알림
     */
    public void sendNotificationStatusUpdate(UUID userId, Long notificationId, String status) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "STATUS_UPDATE");
        update.put("notificationId", notificationId);
        update.put("status", status);
        update.put("timestamp", System.currentTimeMillis());

        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, update);
        log.debug("Sent notification status update to user: {}", userId);
    }

    /**
     * 읽지 않은 알림 개수 업데이트
     */
    public void sendUnreadCountUpdate(UUID userId, Long count) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "UNREAD_COUNT_UPDATE");
        update.put("count", count);
        update.put("timestamp", System.currentTimeMillis());

        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, update);
        log.debug("Sent unread count update to user: {} (count: {})", userId, count);
    }
}