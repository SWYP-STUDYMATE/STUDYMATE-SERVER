package com.studymate.domain.chat.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomListResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.dto.response.ParticipantDto;
import com.studymate.domain.chat.entity.ChatImage;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.ChatRoomParticipant;
import com.studymate.domain.chat.entity.MessageType;
import com.studymate.domain.chat.repository.ChatImageRepository;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomParticipantRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatMessageRepository msgRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate template;
    private final ChatRoomParticipantRepository participantRepo;
    private final AmazonS3Client amazonS3;
    private final ChatImageRepository chatImageRepo;

    @Value("${cloud.ncp.storage.bucket-name}")
    private String bucketName;

    @Override
    public ChatRoomResponse createChatRoom(UUID creatorId, ChatRoomCreateRequest req) {
        ChatRoom room = ChatRoom.builder()
                .roomName(req.roomName())
                .build();
        roomRepo.save(room);

        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());
        room.addParticipant(creator);

        for (UUID id : req.participantIds()) {
            if (id.equals(creatorId)) continue;
            User u = userRepo.findById(id)
                    .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());
            room.addParticipant(u);
        }

        roomRepo.save(room);
        return ChatRoomResponse.from(room);
    }

    @Override
    public void sendMessage(Long roomId, UUID senderId, String message, List<String> imageUrls, String audioUrl, MessageType messageType) {
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());

        boolean inRoom = room.getParticipants().stream()
                .anyMatch(p -> p.getUser().getUserId().equals(senderId));
        if (!inRoom) {
            throw StudymateExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of("방에 속한 사용자만 메시지 전송 가능");
        }

        ChatMessage msg = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .message(message)
                .audioUrl(audioUrl)
                .build();
        msgRepo.save(msg);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                ChatImage chatImage = ChatImage.builder()
                        .chatMessage(msg)
                        .imageUrl(imageUrl)
                        .build();
                chatImageRepo.save(chatImage);
                msg.addImage(chatImage); // 양방향 관계 설정
            }
        }

        ChatMessageResponse resp = ChatMessageResponse.from(msg);
        template.convertAndSend("/sub/chat/room/" + roomId, resp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> listChatRooms(UUID userId) {
        List<ChatRoomParticipant> parts = participantRepo.findByUserUserId(userId);
        return parts.stream()
                .map(p -> {
                    ChatRoom room = p.getRoom();
                    ChatMessage last = msgRepo
                            .findTopByChatRoomOrderByCreatedAtDesc(room)
                            .orElse(null);

                    return new ChatRoomListResponse(
                            room.getId(),
                            room.getRoomName(),
                            room.getParticipants().stream()
                                    .map(cp -> toParticipantDto(cp.getUser()))
                                    .collect(Collectors.toList()),
                            last != null ? last.getMessage() : "",
                            last != null ? last.getCreatedAt() : room.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(Long roomId, UUID userId, int page, int size) {
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());

        boolean inRoom = participantRepo.findByUserUserId(userId).stream()
                .anyMatch(p -> p.getRoom().getId().equals(roomId));
        if (!inRoom) {
            throw StudymateExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of("방에 속한 사용자만 조회 가능");
        }

        return msgRepo.findByChatRoomOrderByCreatedAtAsc(
                        room,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> uploadChatImages(Long roomId, List<MultipartFile> files) {
        // 채팅방 존재 여부 확인 (선택 사항, 보안 강화)
        roomRepo.findById(roomId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());

        return files.stream().map(file -> {
            String key = "chat-images/" + roomId + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                PutObjectRequest request = new PutObjectRequest(
                        bucketName,
                        key,
                        file.getInputStream(),
                        metadata
                ).withCannedAcl(CannedAccessControlList.PublicRead);
                amazonS3.putObject(request);
                return amazonS3.getUrl(bucketName, key).toString();
            } catch (IOException e) {
                throw StudymateExceptionType.SERVER_ERROR.of("이미지 업로드 실패: " + e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    @Override
    public String uploadChatAudio(Long roomId, MultipartFile file) {
        roomRepo.findById(roomId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());

        String key = "chat-audio/" + roomId + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(request);
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (IOException e) {
            throw StudymateExceptionType.SERVER_ERROR.of("오디오 업로드 실패: " + e.getMessage());
        }
    }

    private ParticipantDto toParticipantDto(User user) {
        return new ParticipantDto() {
            @Override public UUID getUserId() { return user.getUserId(); }
            @Override public String getName() { return user.getName(); }
            @Override public String getProfileImage() { return user.getProfileImage(); }
        };
    }
}