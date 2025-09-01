package com.studymate.domain.session.repository;

import com.studymate.domain.session.entity.GroupSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupSessionRepository extends JpaRepository<GroupSession, UUID> {
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.status = :status")
    List<GroupSession> findByStatus(@Param("status") GroupSession.GroupSessionStatus status);
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.hostUserId = :hostUserId")
    List<GroupSession> findByHostUserId(@Param("hostUserId") UUID hostUserId);
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.targetLanguage = :targetLanguage AND gs.languageLevel = :languageLevel AND gs.isPublic = true AND gs.status IN ('SCHEDULED', 'WAITING')")
    List<GroupSession> findAvailableSessionsByLanguage(
        @Param("targetLanguage") String targetLanguage, 
        @Param("languageLevel") String languageLevel
    );
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.topicCategory = :category AND gs.isPublic = true AND gs.status IN ('SCHEDULED', 'WAITING')")
    List<GroupSession> findByTopicCategory(@Param("category") String category);
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.scheduledAt BETWEEN :startTime AND :endTime AND gs.status = 'SCHEDULED'")
    List<GroupSession> findScheduledSessionsInTimeRange(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.joinCode = :joinCode AND gs.status IN ('SCHEDULED', 'WAITING', 'ACTIVE')")
    Optional<GroupSession> findByJoinCode(@Param("joinCode") String joinCode);
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.isPublic = true AND gs.status IN ('SCHEDULED', 'WAITING') AND gs.currentParticipants < gs.maxParticipants ORDER BY gs.scheduledAt ASC")
    Page<GroupSession> findAvailablePublicSessions(Pageable pageable);
    
    @Query("SELECT gs FROM GroupSession gs WHERE gs.sessionTags LIKE %:tag% AND gs.isPublic = true AND gs.status IN ('SCHEDULED', 'WAITING')")
    List<GroupSession> findByTag(@Param("tag") String tag);
    
    @Query("SELECT COUNT(gs) FROM GroupSession gs WHERE gs.hostUserId = :userId AND gs.status = 'ACTIVE'")
    int countActiveSessionsByHost(@Param("userId") UUID userId);
    
    @Query("SELECT AVG(CAST(gsp.rating AS double)) " +
           "FROM GroupSessionParticipant gsp " +
           "JOIN GroupSession gs ON gsp.sessionId = gs.id " +
           "WHERE gs.hostUserId = :userId " +
           "AND gsp.rating IS NOT NULL")
    Double getAverageHostRating(@Param("userId") UUID userId);
}