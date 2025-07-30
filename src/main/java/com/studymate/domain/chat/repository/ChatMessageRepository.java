package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom room);
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom room, Pageable pageable);
}
