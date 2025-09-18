package com.studymate.domain.session.domain.dto.request;

import com.studymate.domain.session.type.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSessionRequest {
    
    @NotBlank(message = "세션 제목은 필수입니다")
    private String title;
    
    private String description;
    
    @NotNull(message = "세션 타입은 필수입니다")
    private SessionType sessionType;
    
    private String languageCode;
    
    private String skillFocus; // SPEAKING, LISTENING, READING, WRITING
    
    private String levelRequirement; // BEGINNER, INTERMEDIATE, ADVANCED
    
    @NotNull(message = "세션 예정 시간은 필수입니다")
    @Future(message = "세션 시간은 현재보다 미래여야 합니다")
    private LocalDateTime scheduledAt;
    
    @NotNull(message = "세션 지속 시간은 필수입니다")
    @Min(value = 15, message = "최소 15분 이상이어야 합니다")
    private Integer durationMinutes;
    
    @Min(value = 1, message = "최소 1명 이상 참여 가능해야 합니다")
    private Integer maxParticipants = 2; // 기본값: 1:1 세션
    
    private Boolean isRecurring = false;
    
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY
    
    private LocalDateTime recurrenceEndDate;
    
    private Boolean isPublic = true;
    
    private String tags;
    
    private String preparationNotes;

    private String webRtcRoomId;

    private String webRtcRoomType; // audio, video
}
