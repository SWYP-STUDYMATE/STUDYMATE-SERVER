package com.studymate.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadMessageSummary {
    
    private UUID roomId;
    
    private String roomName;
    
    private long unreadCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReadAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageAt;
    
    private String lastMessageContent;
    
    private String lastMessageSender;
    
    private String lastMessageType; // TEXT, IMAGE, FILE, etc.
    
    // 전체 안읽은 메시지 통계
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalUnreadSummary {
        private long totalUnreadMessages;
        
        private int unreadRoomsCount;
        
        private Map<UUID, Long> unreadByRoom; // 채팅방별 안읽은 메시지 수
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdatedAt;
        
        public boolean hasUnreadMessages() {
            return totalUnreadMessages > 0;
        }
        
        public String getUnreadSummaryText() {
            if (totalUnreadMessages == 0) {
                return "새 메시지 없음";
            } else if (unreadRoomsCount == 1) {
                return totalUnreadMessages + "개의 새 메시지";
            } else {
                return unreadRoomsCount + "개 채팅방에서 " + totalUnreadMessages + "개의 새 메시지";
            }
        }
    }
}