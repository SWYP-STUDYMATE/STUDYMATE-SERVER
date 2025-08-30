package com.studymate.domain.chat.dto.response;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.MessageType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record ChatMessageResponse(
        Long messageId,
        ParticipantDto sender,
        String message,
        List<String> imageUrls,
        String audioUrl,
        List<ChatFileResponse> files,
        MessageType messageType,
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage msg) {
        MessageType messageType;
        if (msg.isOnlyAudio()) {
            messageType = MessageType.AUDIO;
        } else if (msg.isOnlyImage()) {
            messageType = MessageType.IMAGE;
        } else if (msg.isOnlyMessage()) {
            messageType = MessageType.TEXT;
        } else if (msg.hasFiles() && !msg.hasMessage() && !msg.hasImages() && !msg.hasAudio()) {
            messageType = MessageType.FILE;
        } else {
            messageType = MessageType.MIXED;
        }

        // ChatFile을 ChatFileResponse로 변환
        List<ChatFileResponse> fileResponses = msg.getFiles().stream()
                .filter(file -> !file.isDeleted())
                .map(file -> ChatFileResponse.builder()
                        .fileId(file.getFileId())
                        .originalFilename(file.getOriginalFilename())
                        .fileUrl(file.getFileUrl())
                        .downloadUrl(file.getFileUrl()) // 다운로드 URL은 동일
                        .fileSize(file.getFileSize())
                        .displaySize(file.getDisplaySize())
                        .contentType(file.getContentType())
                        .fileType(file.getFileType().name())
                        .thumbnailUrl(file.getThumbnailUrl())
                        .duration(file.getDuration())
                        .fileExtension(file.getFileExtension())
                        .previewable(file.isPreviewable())
                        .uploadedAt(file.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .sender(new ParticipantDto() {
                    public UUID getUserId() { return msg.getSender().getUserId(); }
                    public String getName() { return msg.getSender().getName(); }
                    public String getProfileImage() { return msg.getSender().getProfileImage(); }
                })
                .message(msg.getMessage())
                .imageUrls(msg.getImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()))
                .audioUrl(msg.getAudioUrl())
                .files(fileResponses)
                .messageType(messageType)
                .sentAt(msg.getCreatedAt())
                .build();
    }
}
