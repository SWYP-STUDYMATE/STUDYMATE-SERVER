package com.studymate.domain.ai.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    
    @NotNull(message = "세션 ID는 필수입니다")
    private UUID sessionId;
    
    @NotBlank(message = "메시지 내용은 필수입니다")
    private String messageContent;
    
    private Boolean requestCorrection = true;
    
    private Boolean requestFeedback = true;
}