package com.studymate.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReadStatusResponse {
    
    private Long messageId;
    
    private int totalReaders;       // 메시지를 읽은 사용자 수
    
    private int totalParticipants;  // 채팅방 전체 참가자 수
    
    private List<ReaderInfo> readers; // 읽은 사용자들 정보
    
    private List<UUID> unreadUserIds; // 읽지 않은 사용자 ID 목록
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime firstReadAt; // 첫 번째 읽음 시간
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReadAt;  // 마지막 읽음 시간
    
    private boolean isFullyRead; // 모든 참가자가 읽었는지 여부
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReaderInfo {
        private UUID userId;
        private String userName;
        private String profileImage;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime readAt;
    }
    
    // Helper methods
    public double getReadPercentage() {
        if (totalParticipants <= 1) return 100.0; // 발신자만 있는 경우
        return (double) totalReaders / (totalParticipants - 1) * 100.0; // 발신자 제외
    }
    
    public boolean hasUnreadUsers() {
        return unreadUserIds != null && !unreadUserIds.isEmpty();
    }
}