package com.studymate.domain.chat.controller;

import com.studymate.common.dto.ResponseDto;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.service.ChatService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/rooms/{roomId}/messages")
public class ChatMessageController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<ResponseDto<List<ChatMessageResponse>>> getChatHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ChatMessageResponse> history =
                chatService.listMessages(roomId, userId, page, size);
        return ResponseEntity.ok(
                ResponseDto.of(history, "채팅 히스토리 조회 완료")
        );
    }
}
