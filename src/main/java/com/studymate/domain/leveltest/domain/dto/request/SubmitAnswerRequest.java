package com.studymate.domain.leveltest.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAnswerRequest {
    
    @NotNull(message = "테스트 ID는 필수입니다")
    private Long testId;
    
    @NotNull(message = "문제 번호는 필수입니다")
    private Integer questionNumber;
    
    private String userAnswer; // 텍스트 답안
    
    private String userAudioUrl; // 음성 답안 (Speaking 테스트용)
    
    private Integer responseTimeSeconds; // 응답 시간
}