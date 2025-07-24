package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByChatRoom_IdAndUser_UserId(Long chatRoomId, UUID userId);
}
