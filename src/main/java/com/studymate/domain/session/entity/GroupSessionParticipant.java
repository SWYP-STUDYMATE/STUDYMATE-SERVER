package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_session_participants")
@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSessionParticipant extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ParticipantStatus status;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    @Column(name = "participation_duration")
    private Integer participationDuration;
    
    @Column(name = "rating")
    private Integer rating;
    
    @Column(name = "feedback", length = 500)
    private String feedback;
    
    @Column(name = "connection_quality")
    private String connectionQuality;
    
    @Column(name = "is_muted")
    private Boolean isMuted;
    
    @Column(name = "is_video_enabled")
    private Boolean isVideoEnabled;
    
    public enum ParticipantStatus {
        INVITED,
        JOINED,
        LEFT,
        KICKED,
        BANNED
    }
}