package com.studymate.domain.session.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class GroupSessionListResponse {
    
    private UUID id;
    private String title;
    private String description;
    private String hostUserName;
    private String hostProfileImage;
    private String topicCategory;
    private String targetLanguage;
    private String languageLevel;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime scheduledAt;
    private Integer sessionDuration;
    private String status;
    private List<String> sessionTags;
    private Double ratingAverage;
    private Integer ratingCount;
    private Boolean canJoin;
    private String timeUntilStart;
}