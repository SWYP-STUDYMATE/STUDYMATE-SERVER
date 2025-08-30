package com.studymate.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileUploadRequest {
    
    @NotNull(message = "채팅방 ID는 필수입니다")
    private UUID roomId;
    
    @Size(max = 500, message = "메시지는 500자를 초과할 수 없습니다")
    private String message;
    
    // 파일 정보는 MultipartFile로 받아서 서비스에서 처리
}