package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.entity.ChatRoomParticipant;
import com.studymate.domain.chat.entity.ChatRoomParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, ChatRoomParticipantId> {
    List<ChatRoomParticipant> findByUserUserId(UUID userId);
    Optional<ChatRoomParticipant> findByRoomIdAndUserUserId(Long roomId, UUID userId);
    
    @Query("SELECT COUNT(p) FROM ChatRoomParticipant p WHERE p.room = :chatRoom AND p.joinedAt IS NOT NULL")
    int countBychatRoomAndJoinedAtIsNotNull(@Param("chatRoom") ChatRoom chatRoom);
    
    @Query("SELECT p.room FROM ChatRoomParticipant p WHERE p.user.userId = :userId")
    List<ChatRoom> findRoomsByUserId(@Param("userId") UUID userId);
}