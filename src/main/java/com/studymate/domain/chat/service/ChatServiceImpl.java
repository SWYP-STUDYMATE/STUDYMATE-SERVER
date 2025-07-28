package com.studymate.domain.chat.service;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.dto.response.ChatRoomListResponse;
import com.studymate.domain.chat.dto.response.ChatRoomResponse;
import com.studymate.domain.chat.dto.response.ParticipantDto;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.ChatRoomParticipant;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomParticipantRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.user.domain.repository.UserRepository; // 수정: UserRepository import
import com.studymate.domain.user.entity.User;                   // 수정: User import
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatMessageRepository msgRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate template;
    private final ChatRoomParticipantRepository participantRepo;

    @Override
    public ChatRoomResponse createChatRoom(UUID creatorId, ChatRoomCreateRequest req) {
        // 1) 방 생성
        ChatRoom room = ChatRoom.builder()
                .roomName(req.roomName())
                .build();
        roomRepo.save(room);

        // 2) 생성자(본인) 무조건 참가
        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());
        room.addParticipant(creator);

        // 3) 나머지 초대된 사람들 추가 (본인은 건너뜀)
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
    public void sendMessage(Long roomId, UUID senderId, String message) {
        // 1) 방 존재 확인
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_CHAT_ROOM.of());
        // 2) 유저 존재 확인
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());
        // 3) 방 권한 확인 (해당 유저가 방에 속해 있는지)
        boolean inRoom = room.getParticipants().stream()
                .anyMatch(p -> p.getUser().getUserId().equals(senderId));
        if (!inRoom) {
            throw StudymateExceptionType.UNAUTHORIZED_TOKEN_EXPIRED.of("방에 속한 사용자만 메시지 전송 가능");
        }
        // 4) 메시지 저장
        ChatMessage msg = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .message(message)
                .build();
        msgRepo.save(msg);
        // 5) 브로드캐스트
        ChatMessageResponse resp = ChatMessageResponse.from(msg);
        template.convertAndSend("/sub/chat/room/" + roomId, resp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> listChatRooms(UUID userId) {
        // 1) 사용자가 속한 방 목록 조회
        List<ChatRoomParticipant> parts = participantRepo.findByUserUserId(userId);

        return parts.stream()
                .map(p -> {
                    ChatRoom room = p.getRoom();
                    // 2) 마지막 메시지 조회
                    ChatMessage last = msgRepo
                            .findTopByChatRoomOrderByCreatedAtDesc(room)
                            .orElse(null);

                    return new ChatRoomListResponse(
                            room.getId(),
                            room.getRoomName(),
                            room.getParticipants().stream()
                                    .map(cp -> toParticipantDto(cp.getUser()))
                                    .toList(),
                            last != null ? last.getMessage() : "",
                            last != null ? last.getCreatedAt() : room.getCreatedAt()
                    );
                })
                .toList();
    }

    // ParticipantDto 매핑 헬퍼
    private ParticipantDto toParticipantDto(User user) {
        return new ParticipantDto() {
            @Override public UUID getUserId() { return user.getUserId(); }
            @Override public String getName() { return user.getName(); }
            @Override public String getProfileImage() { return user.getProfileImage(); }
        };
    }
}
