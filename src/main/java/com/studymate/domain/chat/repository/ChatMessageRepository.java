package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom room);
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom room, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :roomId AND m.sender.userId != :userId")
    long countByRoomIdAndSenderIdNot(@Param("roomId") Long roomId, @Param("userId") UUID userId);
    
    Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);
}
