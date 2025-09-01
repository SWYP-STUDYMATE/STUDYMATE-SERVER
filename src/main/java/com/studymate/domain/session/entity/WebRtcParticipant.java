package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "webrtc_participants", indexes = {
    @Index(name = "idx_webrtc_participant_room", columnList = "webrtc_room_id"),
    @Index(name = "idx_webrtc_participant_user", columnList = "user_id"),
    @Index(name = "idx_webrtc_participant_status", columnList = "connection_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcParticipant extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webrtc_room_id", nullable = false)
    private WebRtcRoom webRtcRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "peer_id", nullable = false)
    private String peerId; // WebRTC peer identifier
    
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private ConnectionStatus connectionStatus;
    
    @Builder.Default
    @Column(name = "is_host", nullable = false)
    private Boolean isHost = false;
    
    @Builder.Default
    @Column(name = "is_moderator", nullable = false)
    private Boolean isModerator = false;
    
    @Builder.Default
    @Column(name = "is_screen_sharing", nullable = false)
    private Boolean isScreenSharing = false;
    
    @Builder.Default
    @Column(name = "is_camera_enabled", nullable = false)
    private Boolean isCameraEnabled = true;
    
    @Builder.Default
    @Column(name = "is_microphone_enabled", nullable = false)
    private Boolean isMicrophoneEnabled = true;
    
    @Column(name = "device_info", columnDefinition = "JSON")
    private String deviceInfo; // Browser, OS, camera/mic capabilities
    
    @Column(name = "connection_quality")
    private String connectionQuality; // EXCELLENT, GOOD, POOR, VERY_POOR
    
    @Column(name = "bandwidth_kbps")
    private Integer bandwidthKbps;
    
    @Column(name = "latency_ms")
    private Integer latencyMs;
    
    @Column(name = "packet_loss_percentage")
    private Double packetLossPercentage;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    @Builder.Default
    @Column(name = "speaking_time_seconds")
    private Integer speakingTimeSeconds = 0;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
    public enum ConnectionStatus {
        CONNECTING("연결 중"),
        CONNECTED("연결됨"),
        RECONNECTING("재연결 중"),
        DISCONNECTED("연결 해제"),
        FAILED("연결 실패");
        
        private final String description;
        
        ConnectionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static WebRtcParticipant createParticipant(WebRtcRoom room, User user, 
                                                     String peerId, boolean isHost) {
        return WebRtcParticipant.builder()
                .webRtcRoom(room)
                .user(user)
                .peerId(peerId)
                .connectionStatus(ConnectionStatus.CONNECTING)
                .isHost(isHost)
                .isModerator(isHost) // 호스트는 기본적으로 모더레이터
                .isCameraEnabled(true)
                .isMicrophoneEnabled(true)
                .isScreenSharing(false)
                .joinedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .speakingTimeSeconds(0)
                .build();
    }
    
    public void connect() {
        this.connectionStatus = ConnectionStatus.CONNECTED;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void disconnect() {
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        this.leftAt = LocalDateTime.now();
    }
    
    public void reconnect() {
        this.connectionStatus = ConnectionStatus.RECONNECTING;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void failConnection() {
        this.connectionStatus = ConnectionStatus.FAILED;
    }
    
    public void toggleCamera() {
        this.isCameraEnabled = !this.isCameraEnabled;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void toggleMicrophone() {
        this.isMicrophoneEnabled = !this.isMicrophoneEnabled;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void startScreenSharing() {
        this.isScreenSharing = true;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void stopScreenSharing() {
        this.isScreenSharing = false;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void updateConnectionQuality(String quality, Integer bandwidth, 
                                       Integer latency, Double packetLoss) {
        this.connectionQuality = quality;
        this.bandwidthKbps = bandwidth;
        this.latencyMs = latency;
        this.packetLossPercentage = packetLoss;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void addSpeakingTime(int seconds) {
        this.speakingTimeSeconds += seconds;
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return this.connectionStatus == ConnectionStatus.CONNECTED ||
               this.connectionStatus == ConnectionStatus.RECONNECTING;
    }
}