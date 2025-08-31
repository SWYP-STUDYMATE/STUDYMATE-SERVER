package com.studymate.domain.ai.domain.dto.request;

import com.studymate.domain.ai.entity.AiSession;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StartAiSessionRequest {
    
    @NotNull(message = "AI 파트너 ID는 필수입니다")
    private UUID aiPartnerId;
    
    private String sessionTitle;
    
    @NotNull(message = "세션 타입은 필수입니다")
    private AiSession.SessionType sessionType;
    
    private String learningObjectives;
}