package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomListResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.entity.MessageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatRoomResponse createChatRoom(UUID creatorId, ChatRoomCreateRequest request);
    void sendMessage(Long roomId, UUID senderId, String message, List<String> imageUrls, String audioUrl, MessageType messageType);
    List<ChatRoomListResponse> listChatRooms(UUID userId);
    List<ChatMessageResponse> listMessages(Long roomId, UUID userId, int page, int size);
    List<String> uploadChatImages(Long roomId, List<MultipartFile> files);
    String uploadChatAudio(Long roomId, MultipartFile file);
}
