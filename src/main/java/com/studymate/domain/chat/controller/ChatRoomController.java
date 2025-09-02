package com.studymate.domain.chat.controller;

import com.studymate.common.dto.ResponseDto;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomListResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
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
@RequestMapping("/api/v1/chat/rooms")
public class ChatRoomController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ResponseDto<ChatRoomResponse>> createChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChatRoomCreateRequest request
    ) {
        UUID creatorId = UUID.fromString(userDetails.getUsername());
        ChatRoomResponse response = chatService.createChatRoom(creatorId, request);
        return ResponseEntity.ok(ResponseDto.of(response, "채팅방이 성공적으로 생성되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<ChatRoomListResponse>>> listChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ChatRoomListResponse> rooms = chatService.listChatRooms(userId);
        return ResponseEntity.ok(ResponseDto.of(rooms, "채팅방 목록 조회 완료"));
    }

    @GetMapping("/public")
    public ResponseEntity<ResponseDto<List<ChatRoomListResponse>>> listPublicChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ChatRoomListResponse> rooms = chatService.listPublicChatRooms(userId);
        return ResponseEntity.ok(ResponseDto.of(rooms, "공개 채팅방 목록 조회 완료"));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ResponseDto<ChatRoomResponse>> joinChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ChatRoomResponse response = chatService.joinChatRoom(roomId, userId);
        return ResponseEntity.ok(ResponseDto.of(response, "채팅방 참여 완료"));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ResponseDto<String>> leaveChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        chatService.leaveChatRoom(roomId, userId);
        return ResponseEntity.ok(ResponseDto.of("채팅방을 나갔습니다.", "채팅방 나가기 완료"));
    }
}
