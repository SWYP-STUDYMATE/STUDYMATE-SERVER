package com.studymate.domain.ai.repository;

import com.studymate.domain.ai.entity.AiSession;
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
public interface AiSessionRepository extends JpaRepository<AiSession, UUID> {
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.userId = :userId ORDER BY ais.startedAt DESC")
    Page<AiSession> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.userId = :userId AND ais.status = :status")
    List<AiSession> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") AiSession.SessionStatus status);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.aiPartnerId = :aiPartnerId ORDER BY ais.startedAt DESC")
    List<AiSession> findByAiPartnerId(@Param("aiPartnerId") UUID aiPartnerId);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.userId = :userId AND ais.aiPartnerId = :aiPartnerId ORDER BY ais.startedAt DESC")
    List<AiSession> findByUserIdAndAiPartnerId(@Param("userId") UUID userId, @Param("aiPartnerId") UUID aiPartnerId);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.userId = :userId AND ais.sessionType = :sessionType ORDER BY ais.startedAt DESC")
    List<AiSession> findByUserIdAndSessionType(@Param("userId") UUID userId, @Param("sessionType") AiSession.SessionType sessionType);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.userId = :userId AND ais.status = 'ACTIVE'")
    Optional<AiSession> findActiveSessionByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(ais) FROM AiSession ais WHERE ais.userId = :userId AND ais.status = 'COMPLETED'")
    int countCompletedSessionsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT SUM(ais.durationMinutes) FROM AiSession ais WHERE ais.userId = :userId AND ais.status = 'COMPLETED'")
    Integer getTotalLearningTimeByUserId(@Param("userId") UUID userId);
    
    default Double getAverageRatingByAiPartnerId(UUID aiPartnerId) {
        // TODO: 실제 평균 평점 계산 로직 구현 필요
        return 4.2; // 임시로 4.2 반환
    }
    
    @Query("SELECT COUNT(ais) FROM AiSession ais WHERE ais.aiPartnerId = :aiPartnerId AND ais.userRating IS NOT NULL")
    int getRatingCountByAiPartnerId(@Param("aiPartnerId") UUID aiPartnerId);
    
    @Query("SELECT ais FROM AiSession ais WHERE ais.startedAt >= :startDate AND ais.endedAt <= :endDate AND ais.userId = :userId")
    List<AiSession> findSessionsInDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}