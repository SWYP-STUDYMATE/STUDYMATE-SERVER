package com.studymate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.chat.dto.request.ChatRoomCreateRequest;
import com.studymate.domain.chat.dto.request.ChatMessageRequest;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.auth.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser1;
    private User testUser2;
    private String accessToken1;
    private String accessToken2;
    private ChatRoom testRoom;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .userId(UUID.randomUUID())
                .name("테스트사용자1")
                .email("test1@example.com")
                .identityType("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser2 = User.builder()
                .userId(UUID.randomUUID())
                .name("테스트사용자2")
                .email("test2@example.com")
                .identityType("NAVER")
                .isOnboardingCompleted(true)
                .build();

        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);

        accessToken1 = jwtUtils.generateAccessToken(testUser1.getUserId());
        accessToken2 = jwtUtils.generateAccessToken(testUser2.getUserId());

        // 테스트 채팅방 생성
        testRoom = ChatRoom.createRoom("테스트채팅방", testUser1, testUser2);
        testRoom = chatRoomRepository.save(testRoom);
    }

    @Test
    @DisplayName("채팅방 목록 조회 통합 테스트")
    void getChatRooms_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/chat/rooms")
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rooms").isArray())
                .andExpect(jsonPath("$.data.rooms[0].name").value("테스트채팅방"));
    }

    @Test
    @DisplayName("채팅방 생성 통합 테스트")
    void createChatRoom_Integration_Success() throws Exception {
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(
                Arrays.asList(testUser2.getUserId()),
                "새로운채팅방"
        );

        mockMvc.perform(post("/api/v1/chat/rooms")
                .header("Authorization", "Bearer " + accessToken1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomName").value("새로운채팅방"));
    }

    @Test
    @DisplayName("채팅 메시지 조회 통합 테스트")
    void getChatMessages_Integration_Success() throws Exception {
        // 테스트 메시지 생성
        ChatMessage testMessage = ChatMessage.builder()
                .chatRoom(testRoom)
                .sender(testUser1)
                .message("안녕하세요!")
                .build();
        chatMessageRepository.save(testMessage);

        mockMvc.perform(get("/api/v1/chat/rooms/" + testRoom.getRoomId() + "/messages")
                .header("Authorization", "Bearer " + accessToken1)
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages[0].content").value("안녕하세요!"));
    }

    @Test
    @DisplayName("채팅 파일 업로드 통합 테스트")
    void uploadChatFile_Integration_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", 
                "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "Hello World".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/chat/files/upload")
                .file(file)
                .param("roomId", testRoom.getRoomId().toString())
                .param("description", "테스트 파일")
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uploadedFiles").isArray());
    }

    @Test
    @DisplayName("메시지 읽음 처리 통합 테스트")
    void markMessageAsRead_Integration_Success() throws Exception {
        // 테스트 메시지 생성
        ChatMessage testMessage = ChatMessage.builder()
                .chatRoom(testRoom)
                .sender(testUser1)
                .message("읽음 테스트 메시지")
                .build();
        testMessage = chatMessageRepository.save(testMessage);

        mockMvc.perform(post("/api/v1/chat/read-status/messages/" + testMessage.getMessageId() + "/read")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("안읽은 메시지 수 조회 통합 테스트")
    void getUnreadMessageCount_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/chat/read-status/rooms/" + testRoom.getRoomId() + "/unread-count")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("채팅방 메시지 일괄 읽음 처리 통합 테스트")
    void markAllRoomMessagesAsRead_Integration_Success() throws Exception {
        mockMvc.perform(post("/api/v1/chat/read-status/rooms/" + testRoom.getRoomId() + "/read-all")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("전체 안읽은 메시지 통계 조회 통합 테스트")
    void getGlobalUnreadSummary_Integration_Success() throws Exception {
        mockMvc.perform(get("/api/v1/chat/read-status/global-unread-summary")
                .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUnreadMessages").isNumber())
                .andExpect(jsonPath("$.data.unreadRoomsCount").isNumber());
    }

    @Test
    @DisplayName("권한 없는 채팅방 접근 시 실패 테스트")
    void accessUnauthorizedChatRoom_Integration_Forbidden() throws Exception {
        UUID unauthorizedRoomId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/chat/rooms/" + unauthorizedRoomId + "/messages")
                .header("Authorization", "Bearer " + accessToken1))
                .andExpect(status().isForbidden());
    }
}