package com.studymate.domain.user.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStatusResponse {
    
    private UUID userId;
    
    private String englishName;
    
    private String profileImageUrl;
    
    private String status; // ONLINE, OFFLINE, STUDYING, AWAY
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSeenAt;
    
    private String deviceInfo;
    
    private boolean isStudying;
    
    private UUID currentSessionId;
    
    private String location;
    
    private String nativeLanguage;
    
    // Helper methods
    public boolean isOnline() {
        return "ONLINE".equals(status) || "STUDYING".equals(status);
    }
    
    public String getLastSeenText() {
        if (isOnline()) {
            return "현재 온라인";
        }
        
        if (lastSeenAt == null) {
            return "알 수 없음";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = java.time.Duration.between(lastSeenAt, now).toMinutes();
        
        if (minutesAgo < 1) {
            return "방금 전";
        } else if (minutesAgo < 60) {
            return minutesAgo + "분 전";
        } else if (minutesAgo < 1440) { // 24 hours
            return (minutesAgo / 60) + "시간 전";
        } else {
            return (minutesAgo / 1440) + "일 전";
        }
    }
}