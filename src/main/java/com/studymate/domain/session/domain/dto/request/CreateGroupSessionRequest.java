package com.studymate.domain.session.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateGroupSessionRequest {
    
    @NotBlank(message = "세션 제목은 필수입니다")
    private String title;
    
    private String description;
    
    @NotBlank(message = "주제 카테고리는 필수입니다")
    private String topicCategory;
    
    @NotBlank(message = "대상 언어는 필수입니다")
    private String targetLanguage;
    
    @NotBlank(message = "언어 수준은 필수입니다")
    private String languageLevel;
    
    @NotNull(message = "최대 참가자 수는 필수입니다")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다")
    @Max(value = 10, message = "최대 10명까지 가능합니다")
    private Integer maxParticipants;
    
    @NotNull(message = "예약 시간은 필수입니다")
    private LocalDateTime scheduledAt;
    
    @NotNull(message = "세션 시간은 필수입니다")
    @Min(value = 15, message = "최소 15분 이상이어야 합니다")
    @Max(value = 180, message = "최대 180분까지 가능합니다")
    private Integer sessionDuration;
    
    private List<String> sessionTags;
    
    @NotNull(message = "공개 여부는 필수입니다")
    private Boolean isPublic;
}