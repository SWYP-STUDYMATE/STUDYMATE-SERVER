package com.studymate.domain.webrtc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ActiveRoomSummaryResponse {

    private String roomId;
    private String roomType;
    private String status;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private Map<String, Object> metadata;
    private SessionSummary session;

    @Data
    @Builder
    @AllArgsConstructor
    public static class SessionSummary {
        private Long sessionId;
        private String title;
        private String description;
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
        private String languageCode;
        private String sessionStatus;
        private String hostName;
        private Integer waitlistCount;
    }
}
