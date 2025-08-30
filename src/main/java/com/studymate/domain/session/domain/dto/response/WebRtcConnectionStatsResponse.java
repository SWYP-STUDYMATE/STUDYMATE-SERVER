package com.studymate.domain.session.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcConnectionStatsResponse {

    private UUID sessionId;
    private UUID roomId;
    private LocalDateTime timestamp;
    
    // 전체 세션 통계
    private Integer totalParticipants;
    private Integer activeParticipants;
    private Integer connectedParticipants;
    private Long sessionDurationMinutes;
    
    // 연결 품질 통계
    private String overallConnectionQuality; // excellent, good, fair, poor
    private Double averageSignalStrength; // 1.0 - 5.0
    private Integer connectionDropouts;
    private Integer reconnectionAttempts;
    private Integer successfulReconnections;
    
    // 오디오 통계
    private AudioStats audioStats;
    
    // 비디오 통계
    private VideoStats videoStats;
    
    // 네트워크 통계
    private NetworkStats networkStats;
    
    // 참가자별 상세 통계
    private List<ParticipantStats> participantStats;
    
    // 세션 품질 메트릭
    private Double sessionQualityScore; // 0.0 - 100.0
    private List<String> qualityIssues;
    private List<String> performanceRecommendations;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioStats {
        private Integer packetsTransmitted;
        private Integer packetsReceived;
        private Integer packetsLost;
        private Double packetLossPercentage;
        private Double averageJitter; // ms
        private Double averageLatency; // ms
        private String audioCodec;
        private Integer bitrate; // kbps
        private Integer audioLevel; // 0-100
        private Boolean echoCancellationEnabled;
        private Boolean noiseSuppressionEnabled;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoStats {
        private Integer framesTransmitted;
        private Integer framesReceived;
        private Integer framesDropped;
        private Double frameRate; // fps
        private String resolution; // 1920x1080
        private String videoCodec;
        private Integer bitrate; // kbps
        private Double qualityLimitationReason;
        private Integer bandwidthLimitationCount;
        private Integer cpuLimitationCount;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkStats {
        private Long totalBytesSent;
        private Long totalBytesReceived;
        private Double currentRoundTripTime; // ms
        private Double averageRoundTripTime; // ms
        private String connectionType; // wifi, ethernet, cellular
        private Double availableBandwidth; // Mbps
        private Double usedBandwidth; // Mbps
        private Integer firCount; // Full Intra Request count
        private Integer pliCount; // Picture Loss Indication count
        private Integer nackCount; // Negative ACK count
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantStats {
        private UUID userId;
        private String displayName;
        private LocalDateTime joinedAt;
        private LocalDateTime leftAt;
        private Long participationDurationMinutes;
        private Integer speakingTimeSeconds;
        private Double speakingTimePercentage;
        private String connectionQuality;
        private Integer signalStrength;
        private Integer connectionDrops;
        private Boolean hadVideoIssues;
        private Boolean hadAudioIssues;
        private Boolean hadNetworkIssues;
        private Double participationScore;
        private Map<String, Object> technicalMetrics;
    }
    
    // Helper methods
    
    public boolean isSessionHealthy() {
        return "excellent".equals(overallConnectionQuality) || "good".equals(overallConnectionQuality);
    }
    
    public boolean hasSignificantIssues() {
        return connectionDropouts > 5 || 
               (networkStats != null && networkStats.averageRoundTripTime > 500) ||
               (audioStats != null && audioStats.packetLossPercentage > 5.0);
    }
    
    public String getSessionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("세션 품질: %.1f/100, ", sessionQualityScore));
        summary.append(String.format("참가자: %d명, ", totalParticipants));
        summary.append(String.format("세션 시간: %d분, ", sessionDurationMinutes));
        summary.append(String.format("연결 상태: %s", overallConnectionQuality));
        
        if (hasSignificantIssues()) {
            summary.append(" (품질 문제 감지됨)");
        }
        
        return summary.toString();
    }
    
    public List<String> getOptimizationSuggestions() {
        return List.of(
            networkStats != null && networkStats.averageRoundTripTime > 300 ? 
                "네트워크 연결 상태를 확인해 주세요" : null,
            audioStats != null && audioStats.packetLossPercentage > 3.0 ? 
                "오디오 품질 개선을 위해 마이크 설정을 확인해 주세요" : null,
            videoStats != null && videoStats.framesDropped > 100 ? 
                "비디오 품질 개선을 위해 해상도를 낮춰 보세요" : null,
            connectionDropouts > 3 ? 
                "안정적인 네트워크 환경으로 이동해 주세요" : null
        ).stream().filter(java.util.Objects::nonNull).toList();
    }
}