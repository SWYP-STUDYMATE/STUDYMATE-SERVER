package com.studymate.domain.session.domain.dto.response;

import com.studymate.domain.session.type.SessionStatus;
import com.studymate.domain.session.type.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionResponse {
    private Long sessionId;
    private UUID hostUserId;
    private String hostUserName;
    private String hostUserProfileImage;
    private UUID guestUserId;
    private String guestUserName;
    private String guestUserProfileImage;
    private String title;
    private String description;
    private SessionType sessionType;
    private String languageCode;
    private String skillFocus;
    private String levelRequirement;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private SessionStatus status;
    private String meetingUrl;
    private Boolean isRecurring;
    private String recurrencePattern;
    private LocalDateTime recurrenceEndDate;
    private Boolean isPublic;
    private String tags;
    private String preparationNotes;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Boolean canJoin;
    private Boolean isHost;
    private Boolean isParticipant;
}