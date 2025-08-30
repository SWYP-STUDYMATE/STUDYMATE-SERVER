package com.studymate.domain.session.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcRoomResponse {
    
    private UUID roomId;
    private Long sessionId;
    private String status;
    private String signalingServerUrl;
    private List<Map<String, Object>> iceServers;
    
    private Integer maxParticipants;
    private Integer currentParticipants;
    
    private Boolean isRecordingEnabled;
    private String recordingUrl;
    
    // 품질 및 기능 설정
    private Map<String, Object> qualityConfig;
    private Map<String, Boolean> featuresEnabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endedAt;
    
    // 참가자 정보 (간단한 형태)
    private List<ParticipantInfo> participants;
    
    // 룸 통계
    private RoomStatistics statistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private UUID userId;
        private String userName;
        private String peerId;
        private Boolean isHost;
        private Boolean isModerator;
        private String connectionStatus;
        private Boolean cameraEnabled;
        private Boolean microphoneEnabled;
        private Boolean screenSharing;
        private String connectionQuality;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime joinedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomStatistics {
        private Integer totalConnections;
        private Integer activeConnections;
        private Integer failedConnections;
        private Double averageLatency;
        private Double averageBitrate;
        private Double averagePacketLoss;
        private Long totalDataTransferred;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdated;
    }
    
    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public boolean canAcceptNewParticipant() {
        return isActive() && currentParticipants < maxParticipants;
    }
    
    public boolean hasRecording() {
        return recordingUrl != null && !recordingUrl.isEmpty();
    }
}