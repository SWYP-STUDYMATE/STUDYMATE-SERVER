package com.studymate.domain.ai.repository;

import com.studymate.domain.ai.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    
    @Query("SELECT aim FROM AiMessage aim WHERE aim.sessionId = :sessionId ORDER BY aim.createdAt ASC")
    List<AiMessage> findBySessionIdOrderByCreatedAt(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT aim FROM AiMessage aim WHERE aim.sessionId = :sessionId AND aim.senderType = :senderType ORDER BY aim.createdAt ASC")
    List<AiMessage> findBySessionIdAndSenderType(
        @Param("sessionId") UUID sessionId, 
        @Param("senderType") AiMessage.SenderType senderType
    );
    
    @Query("SELECT aim FROM AiMessage aim WHERE aim.sessionId = :sessionId AND aim.messageType = :messageType ORDER BY aim.createdAt ASC")
    List<AiMessage> findBySessionIdAndMessageType(
        @Param("sessionId") UUID sessionId,
        @Param("messageType") AiMessage.MessageType messageType
    );
    
    @Query("SELECT COUNT(aim) FROM AiMessage aim WHERE aim.sessionId = :sessionId")
    int countMessagesBySessionId(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT COUNT(aim) FROM AiMessage aim WHERE aim.sessionId = :sessionId AND aim.senderType = 'USER'")
    int countUserMessagesBySessionId(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT AVG(aim.responseTimeMs) FROM AiMessage aim WHERE aim.sessionId = :sessionId AND aim.senderType = 'AI_PARTNER'")
    Double getAverageResponseTime(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT SUM(aim.tokensUsed) FROM AiMessage aim WHERE aim.sessionId = :sessionId")
    Integer getTotalTokensUsed(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT aim FROM AiMessage aim WHERE aim.sessionId = :sessionId AND aim.correctionsJson IS NOT NULL ORDER BY aim.createdAt ASC")
    List<AiMessage> findCorrectionsForSession(@Param("sessionId") UUID sessionId);
}