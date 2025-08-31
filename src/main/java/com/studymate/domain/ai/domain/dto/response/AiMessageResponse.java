package com.studymate.domain.ai.domain.dto.response;

import com.studymate.domain.ai.entity.AiMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AiMessageResponse {
    
    private UUID id;
    private UUID sessionId;
    private AiMessage.SenderType senderType;
    private String messageContent;
    private String originalText;
    private String correctedText;
    private List<TextCorrection> corrections;
    private String grammarFeedback;
    private List<String> vocabularySuggestions;
    private String pronunciationFeedback;
    private Double confidenceScore;
    private Long responseTimeMs;
    private AiMessage.MessageType messageType;
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    public static class TextCorrection {
        private String original;
        private String corrected;
        private String explanation;
        private String correctionType;
        private Integer position;
    }
}