package com.studymate.domain.chat.controller;

import com.studymate.common.dto.ResponseDto;
import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;

import com.studymate.domain.chat.entity.MessageType;
import com.studymate.domain.chat.service.ChatService;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(
            Authentication authentication,
            @Payload ChatMessageRequest request
    ) {
        // 인증/Principal 체크
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw StudymateExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of(
                    "인증된 사용자만 메시지 전송 가능");
        }

        // CustomUserDetails 꺼내기
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        UUID userId = UUID.fromString(userDetails.getUsername());

        String finalAudioUrl = null;
        if (request.audioData() != null && request.messageType() == MessageType.AUDIO) {
            // Base64 → S3 업로드
            finalAudioUrl = chatService.uploadChatAudioFromBase64(
                    request.roomId(),
                    request.audioData()
            );
        }


        // 실제 메시지 전송 로직
        chatService.sendMessage(
                request.roomId(),
                userId,
                request.message(),
                request.imageUrls(),
                finalAudioUrl,
                request.messageType()
        );
    }

    @PostMapping("/api/v1/chat/rooms/{roomId}/images")
    public ResponseDto<List<String>> uploadChatImage(
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<String> imageUrls = chatService.uploadChatImages(roomId, files);
        return ResponseDto.of(imageUrls, "이미지 업로드 성공");
    }

    @PostMapping("/api/v1/chat/rooms/{roomId}/audio")
    public ResponseDto<String> uploadChatAudio(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file
    ) {
        String audioUrl = chatService.uploadChatAudio(roomId, file);
        return ResponseDto.of(audioUrl, "오디오 업로드 성공");
    }
}
