package com.studymate.domain.session.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSessionRequest {
    
    @NotNull(message = "세션 ID는 필수입니다")
    private Long sessionId;
    
    private String bookingMessage; // 예약 시 호스트에게 보낼 메시지
}