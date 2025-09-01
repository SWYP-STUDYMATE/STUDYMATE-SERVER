package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "webrtc_rooms", indexes = {
    @Index(name = "idx_webrtc_room_id", columnList = "room_id"),
    @Index(name = "idx_webrtc_session_id", columnList = "session_id"),
    @Index(name = "idx_webrtc_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcRoom extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false, unique = true)
    private UUID roomId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WebRtcRoomStatus status;
    
    @Column(name = "signaling_server_url")
    private String signalingServerUrl;
    
    @Column(name = "turn_server_url")
    private String turnServerUrl;
    
    @Column(name = "turn_username")
    private String turnUsername;
    
    @Column(name = "turn_credential")
    private String turnCredential;
    
    @Column(name = "ice_servers", columnDefinition = "JSON")
    private String iceServers; // JSON array of ICE servers
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Builder.Default
    @Column(name = "current_participants")
    private Integer currentParticipants = 0;
    
    @Builder.Default
    @Column(name = "is_recording_enabled")
    private Boolean isRecordingEnabled = false;
    
    @Column(name = "recording_url")
    private String recordingUrl;
    
    @Column(name = "quality_config", columnDefinition = "JSON")
    private String qualityConfig; // JSON object with video/audio quality settings
    
    @Column(name = "features_enabled", columnDefinition = "JSON")
    private String featuresEnabled; // JSON object: screen share, chat, recording, etc.
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @OneToMany(mappedBy = "webRtcRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WebRtcParticipant> participants = new ArrayList<>();
    
    @OneToMany(mappedBy = "webRtcRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WebRtcConnection> connections = new ArrayList<>();
    
    public enum WebRtcRoomStatus {
        CREATED("생성됨"),
        ACTIVE("활성"),
        ENDED("종료됨"),
        ERROR("오류");
        
        private final String description;
        
        WebRtcRoomStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static WebRtcRoom createRoom(Session session, String signalingServerUrl, 
                                       String turnServerUrl, Integer maxParticipants) {
        return WebRtcRoom.builder()
                .roomId(UUID.randomUUID())
                .session(session)
                .status(WebRtcRoomStatus.CREATED)
                .signalingServerUrl(signalingServerUrl)
                .turnServerUrl(turnServerUrl)
                .maxParticipants(maxParticipants)
                .currentParticipants(0)
                .isRecordingEnabled(false)
                .build();
    }
    
    public void startRoom() {
        this.status = WebRtcRoomStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }
    
    public void endRoom() {
        this.status = WebRtcRoomStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }
    
    public void setError() {
        this.status = WebRtcRoomStatus.ERROR;
    }
    
    public void addParticipant() {
        this.currentParticipants++;
    }
    
    public void removeParticipant() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }
    
    public boolean canAcceptNewParticipant() {
        return this.currentParticipants < this.maxParticipants && 
               this.status == WebRtcRoomStatus.ACTIVE;
    }
    
    public boolean isActive() {
        return this.status == WebRtcRoomStatus.ACTIVE;
    }
}