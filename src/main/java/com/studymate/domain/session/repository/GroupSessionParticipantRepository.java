package com.studymate.domain.session.repository;

import com.studymate.domain.session.entity.GroupSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupSessionParticipantRepository extends JpaRepository<GroupSessionParticipant, UUID> {
    
    @Query("SELECT gsp FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId")
    List<GroupSessionParticipant> findBySessionId(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT gsp FROM GroupSessionParticipant gsp WHERE gsp.userId = :userId")
    List<GroupSessionParticipant> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT gsp FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId AND gsp.userId = :userId")
    Optional<GroupSessionParticipant> findBySessionIdAndUserId(
        @Param("sessionId") UUID sessionId, 
        @Param("userId") UUID userId
    );
    
    @Query("SELECT gsp FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId AND gsp.status = :status")
    List<GroupSessionParticipant> findBySessionIdAndStatus(
        @Param("sessionId") UUID sessionId, 
        @Param("status") GroupSessionParticipant.ParticipantStatus status
    );
    
    @Query("SELECT COUNT(gsp) FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId AND gsp.status = 'JOINED'")
    int countActiveParticipants(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT gsp FROM GroupSessionParticipant gsp WHERE gsp.userId = :userId AND gsp.status = 'JOINED'")
    List<GroupSessionParticipant> findActiveSessionsByUser(@Param("userId") UUID userId);
    
    @Query("SELECT AVG(CAST(gsp.rating AS double)) FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId AND gsp.rating IS NOT NULL")
    Double getAverageSessionRating(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT COUNT(gsp) FROM GroupSessionParticipant gsp WHERE gsp.sessionId = :sessionId AND gsp.rating IS NOT NULL")
    int getSessionRatingCount(@Param("sessionId") UUID sessionId);
}