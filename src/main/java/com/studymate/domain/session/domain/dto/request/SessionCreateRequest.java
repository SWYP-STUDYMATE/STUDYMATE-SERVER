package com.studymate.domain.session.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreateRequest {
    
    private String title;
    private String description;
    private String targetLanguage;
    private String sessionType;
    private Integer maxParticipants;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private List<String> topics;
    private String difficulty;
    private Boolean isPrivate;
    private String password;
}