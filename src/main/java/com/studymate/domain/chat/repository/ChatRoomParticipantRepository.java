package com.studymate.domain.chat.repository;


import com.studymate.domain.chat.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Void> {
    List<ChatRoomParticipant> findByUserUserId(UUID userId);
}