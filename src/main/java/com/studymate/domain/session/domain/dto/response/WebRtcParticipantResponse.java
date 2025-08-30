package com.studymate.domain.session.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcParticipantResponse {

    private UUID userId;
    private String displayName;
    private String profileImageUrl;
    private String connectionId;
    
    // 참가자 상태
    private String status; // joining, connected, disconnected
    private LocalDateTime joinedAt;
    private LocalDateTime lastActiveAt;
    
    // 권한 및 역할
    private Boolean isHost;
    private Boolean isModerator;
    private Boolean canSpeak;
    private Boolean canViewScreen;
    private Boolean canControlMedia;
    
    // 미디어 상태
    private Boolean isCameraEnabled;
    private Boolean isMicrophoneEnabled;
    private Boolean isScreenSharing;
    private Boolean isSpeaking;
    
    // 연결 상태
    private String connectionQuality; // excellent, good, fair, poor
    private Integer signalStrength; // 1-5
    private Boolean isConnected;
    private String networkType; // wifi, cellular, ethernet
    
    // 세션 통계
    private Integer speakingTimeSeconds;
    private Integer totalParticipationMinutes;
    private LocalDateTime lastSpokeAt;
    private Integer messagesCount;
    
    // 기술적 정보
    private String userAgent;
    private String deviceInfo;
    private String browserInfo;
    private String ipAddress; // 보안상 마스킹된 IP
    
    // 언어 학습 정보
    private String nativeLanguage;
    private String learningLanguage;
    private String languageLevel;
    
    // 세션별 선호도
    private Boolean preferVideoCall;
    private Boolean preferAudioOnly;
    private String sessionGoal; // practice, tutoring, casual_chat
    
    public boolean isActive() {
        return "connected".equals(this.status) && Boolean.TRUE.equals(this.isConnected);
    }
    
    public boolean hasMediaEnabled() {
        return Boolean.TRUE.equals(this.isCameraEnabled) || Boolean.TRUE.equals(this.isMicrophoneEnabled);
    }
    
    public boolean canModerate() {
        return Boolean.TRUE.equals(this.isHost) || Boolean.TRUE.equals(this.isModerator);
    }
    
    public String getConnectionStatusDescription() {
        if (!Boolean.TRUE.equals(isConnected)) {
            return "연결 끊김";
        }
        
        return switch (connectionQuality != null ? connectionQuality : "unknown") {
            case "excellent" -> "연결 상태 우수";
            case "good" -> "연결 상태 양호";
            case "fair" -> "연결 상태 보통";
            case "poor" -> "연결 상태 불안정";
            default -> "연결 상태 불명";
        };
    }
    
    public double getParticipationScore() {
        double score = 0.0;
        
        // 발언 시간 기반 점수 (40%)
        if (speakingTimeSeconds != null && totalParticipationMinutes != null && totalParticipationMinutes > 0) {
            score += (speakingTimeSeconds / (totalParticipationMinutes * 60.0)) * 40;
        }
        
        // 연결 안정성 기반 점수 (30%)
        if ("excellent".equals(connectionQuality)) score += 30;
        else if ("good".equals(connectionQuality)) score += 25;
        else if ("fair".equals(connectionQuality)) score += 15;
        else if ("poor".equals(connectionQuality)) score += 5;
        
        // 참여도 기반 점수 (30%)
        if (Boolean.TRUE.equals(isCameraEnabled)) score += 15;
        if (Boolean.TRUE.equals(isMicrophoneEnabled)) score += 15;
        
        return Math.min(100.0, score);
    }
}