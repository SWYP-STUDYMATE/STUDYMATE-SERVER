package com.studymate.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageRetryService 단위 테스트")
class MessageRetryServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private MessageRetryService messageRetryService;

    private UUID userId;
    private Long roomId;
    private ChatMessage chatMessage;
    private User sender;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roomId = 1L;
        
        sender = User.builder()
                .userId(userId)
                .username("testUser")
                .build();
        
        chatRoom = ChatRoom.builder()
                .id(roomId)
                .build();
        
        chatMessage = ChatMessage.builder()
                .id(1L)
                .content("테스트 메시지")
                .sender(sender)
                .chatRoom(chatRoom)
                .createdAt(LocalDateTime.now())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("메시지 재시도 큐에 추가")
    void addToRetryQueue_Success() throws Exception {
        // Given
        String expectedJson = "{\"messageId\":1,\"content\":\"테스트 메시지\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        // When
        messageRetryService.addToRetryQueue(chatMessage);

        // Then
        String retryKey = "message_retry:" + roomId + ":" + userId;
        verify(listOperations).rightPush(retryKey, expectedJson);
        verify(redisTemplate).expire(retryKey, Duration.ofDays(7));
    }

    @Test
    @DisplayName("재시도 큐에서 메시지 처리")
    void processRetryQueue_Success() throws Exception {
        // Given
        String retryKey = "message_retry:" + roomId + ":" + userId;
        String messageJson = "{\"messageId\":1,\"content\":\"테스트 메시지\"}";
        
        when(listOperations.leftPop(retryKey)).thenReturn(messageJson);
        
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", 1L);
        messageData.put("content", "테스트 메시지");
        
        when(objectMapper.readValue(eq(messageJson), eq(Map.class))).thenReturn(messageData);
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage));

        // When
        List<ChatMessage> result = messageRetryService.processRetryQueue(roomId, userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(chatMessage);
        verify(listOperations).leftPop(retryKey);
    }

    @Test
    @DisplayName("재시도 큐가 비어있을 때")
    void processRetryQueue_EmptyQueue() {
        // Given
        String retryKey = "message_retry:" + roomId + ":" + userId;
        when(listOperations.leftPop(retryKey)).thenReturn(null);

        // When
        List<ChatMessage> result = messageRetryService.processRetryQueue(roomId, userId);

        // Then
        assertThat(result).isEmpty();
        verify(listOperations).leftPop(retryKey);
    }

    @Test
    @DisplayName("메시지 재시도 횟수 증가")
    void incrementRetryCount_Success() {
        // Given
        Long messageId = 1L;

        // When
        messageRetryService.incrementRetryCount(messageId);

        // Then
        String countKey = "message_retry_count:" + messageId;
        verify(valueOperations).increment(countKey);
        verify(redisTemplate).expire(countKey, Duration.ofHours(24));
    }

    @Test
    @DisplayName("메시지 재시도 횟수 조회")
    void getRetryCount_Success() {
        // Given
        Long messageId = 1L;
        String countKey = "message_retry_count:" + messageId;
        when(valueOperations.get(countKey)).thenReturn("2");

        // When
        int result = messageRetryService.getRetryCount(messageId);

        // Then
        assertThat(result).isEqualTo(2);
        verify(valueOperations).get(countKey);
    }

    @Test
    @DisplayName("메시지 재시도 횟수 조회 - 카운트 없음")
    void getRetryCount_NoCount() {
        // Given
        Long messageId = 1L;
        String countKey = "message_retry_count:" + messageId;
        when(valueOperations.get(countKey)).thenReturn(null);

        // When
        int result = messageRetryService.getRetryCount(messageId);

        // Then
        assertThat(result).isEqualTo(0);
        verify(valueOperations).get(countKey);
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 확인")
    void isMaxRetryExceeded_True() {
        // Given
        Long messageId = 1L;
        String countKey = "message_retry_count:" + messageId;
        when(valueOperations.get(countKey)).thenReturn("4");

        // When
        boolean result = messageRetryService.isMaxRetryExceeded(messageId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("최대 재시도 횟수 미초과 확인")
    void isMaxRetryExceeded_False() {
        // Given
        Long messageId = 1L;
        String countKey = "message_retry_count:" + messageId;
        when(valueOperations.get(countKey)).thenReturn("2");

        // When
        boolean result = messageRetryService.isMaxRetryExceeded(messageId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("오프라인 메시지 저장")
    void storeOfflineMessage_Success() throws Exception {
        // Given
        String expectedJson = "{\"messageId\":1,\"content\":\"테스트 메시지\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        // When
        messageRetryService.storeOfflineMessage(userId, chatMessage);

        // Then
        String offlineKey = "offline_messages:" + userId;
        verify(listOperations).rightPush(offlineKey, expectedJson);
        verify(redisTemplate).expire(offlineKey, Duration.ofDays(7));
    }

    @Test
    @DisplayName("오프라인 메시지 조회")
    void getOfflineMessages_Success() throws Exception {
        // Given
        String offlineKey = "offline_messages:" + userId;
        List<String> messageJsonList = Arrays.asList(
            "{\"messageId\":1,\"content\":\"메시지1\"}",
            "{\"messageId\":2,\"content\":\"메시지2\"}"
        );
        
        when(listOperations.range(offlineKey, 0, -1)).thenReturn(messageJsonList);
        
        Map<String, Object> messageData1 = new HashMap<>();
        messageData1.put("messageId", 1L);
        messageData1.put("content", "메시지1");
        
        Map<String, Object> messageData2 = new HashMap<>();
        messageData2.put("messageId", 2L);
        messageData2.put("content", "메시지2");
        
        when(objectMapper.readValue(messageJsonList.get(0), Map.class)).thenReturn(messageData1);
        when(objectMapper.readValue(messageJsonList.get(1), Map.class)).thenReturn(messageData2);
        
        ChatMessage message1 = ChatMessage.builder().id(1L).content("메시지1").build();
        ChatMessage message2 = ChatMessage.builder().id(2L).content("메시지2").build();
        
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message1));
        when(chatMessageRepository.findById(2L)).thenReturn(Optional.of(message2));

        // When
        List<ChatMessage> result = messageRetryService.getOfflineMessages(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("오프라인 메시지 삭제")
    void clearOfflineMessages_Success() {
        // When
        messageRetryService.clearOfflineMessages(userId);

        // Then
        String offlineKey = "offline_messages:" + userId;
        verify(redisTemplate).delete(offlineKey);
    }

    @Test
    @DisplayName("사용자의 미읽은 메시지 개수 조회")
    void getUnreadMessageCount_Success() {
        // Given
        when(chatMessageRepository.countByRoomIdAndSenderIdNot(roomId, userId)).thenReturn(5);

        // When
        int result = messageRetryService.getUnreadMessageCount(roomId, userId);

        // Then
        assertThat(result).isEqualTo(5);
        verify(chatMessageRepository).countByRoomIdAndSenderIdNot(roomId, userId);
    }
}