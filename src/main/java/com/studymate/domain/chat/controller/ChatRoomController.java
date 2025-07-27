package com.studymate.domain.chat.controller;

import com.studymate.common.dto.ResponseDto;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ResponseDto<ChatRoomResponse>> createChatRoom(
            @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatService.createChatRoom(request);
        return ResponseEntity.ok(ResponseDto.of(response, "채팅방이 성공적으로 생성되었습니다."));
    }
}
