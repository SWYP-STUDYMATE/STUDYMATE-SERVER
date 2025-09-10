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
@Table(name = "group_sessions")
@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSession extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_title", length = 200)
    private String title;
    
    @Column(name = "session_description", length = 1000)
    private String description;
    
    @Column(name = "host_user_id", nullable = false)
    private UUID hostUserId;
    
    @Column(name = "topic_category", length = 100)
    private String topicCategory;
    
    @Column(name = "target_language", length = 10)
    private String targetLanguage;
    
    @Column(name = "language_level", length = 20)
    private String languageLevel;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "current_participants")
    private Integer currentParticipants;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "session_duration")
    private Integer sessionDuration;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GroupSessionStatus status;
    
    @Column(name = "room_id", length = 100)
    private String roomId;
    
    @Column(name = "session_tags", length = 500)
    private String sessionTags;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    @Column(name = "join_code", length = 10)
    private String joinCode;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "rating_average")
    private Double ratingAverage;
    
    @Column(name = "rating_count")
    private Integer ratingCount;
    
    public enum GroupSessionStatus {
        SCHEDULED,
        WAITING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}