package com.studymate.domain.chat.repository;


import com.studymate.domain.chat.entity.ChatRoomParticipant;
import com.studymate.domain.chat.entity.ChatRoomParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, ChatRoomParticipantId> {
    List<ChatRoomParticipant> findByUserUserId(UUID userId);
    Optional<ChatRoomParticipant> findByRoomIdAndUserUserId(Long roomId, UUID userId);
}