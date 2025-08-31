package com.studymate.domain.session.domain.dto.response;

import com.studymate.domain.session.entity.GroupSession;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GroupSessionResponse {
    
    private UUID id;
    private String title;
    private String description;
    private UUID hostUserId;
    private String hostUserName;
    private String hostProfileImage;
    private String topicCategory;
    private String targetLanguage;
    private String languageLevel;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime scheduledAt;
    private Integer sessionDuration;
    private GroupSession.GroupSessionStatus status;
    private String roomId;
    private List<String> sessionTags;
    private Boolean isPublic;
    private String joinCode;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Double ratingAverage;
    private Integer ratingCount;
    private List<ParticipantInfo> participants;
    private Boolean canJoin;
    private String joinMessage;
    
    @Data
    @Builder
    public static class ParticipantInfo {
        private UUID userId;
        private String userName;
        private String profileImage;
        private String status;
        private LocalDateTime joinedAt;
        private Boolean isMuted;
        private Boolean isVideoEnabled;
    }
}