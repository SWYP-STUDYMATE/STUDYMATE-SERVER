package com.studymate.domain.matching.domain.repository;

import com.studymate.domain.matching.entity.MatchingQueue;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingQueueRepository extends JpaRepository<MatchingQueue, Long> {

    /**
     * 특정 사용자와 상태로 대기열 항목 조회
     */
    Optional<MatchingQueue> findByUserAndStatus(User user, MatchingQueue.QueueStatus status);

    /**
     * 특정 상태의 대기열 항목 수 조회
     */
    long countByStatus(MatchingQueue.QueueStatus status);

    /**
     * 특정 시간 이전에 참가한 대기열 항목 수 조회 (대기 순서 계산용)
     */
    long countByStatusAndJoinedAtBefore(MatchingQueue.QueueStatus status, LocalDateTime joinedAt);

    /**
     * 특정 세션 타입과 상태로 대기열 항목 조회
     */
    List<MatchingQueue> findBySessionTypeAndStatusOrderByPriorityScoreDescJoinedAtAsc(
            MatchingQueue.SessionType sessionType, MatchingQueue.QueueStatus status);

    /**
     * 우선순위 점수와 참가 시간 순으로 대기열 조회
     */
    @Query("SELECT mq FROM MatchingQueue mq WHERE mq.status = :status " +
           "ORDER BY mq.priorityScore DESC, mq.joinedAt ASC")
    List<MatchingQueue> findByStatusOrderByPriorityScoreDescJoinedAtAsc(@Param("status") MatchingQueue.QueueStatus status);

    /**
     * 만료된 대기열 항목 조회
     */
    @Query("SELECT mq FROM MatchingQueue mq WHERE mq.status = :status " +
           "AND mq.joinedAt < :expiryTime")
    List<MatchingQueue> findExpiredQueueEntries(@Param("status") MatchingQueue.QueueStatus status, 
                                              @Param("expiryTime") LocalDateTime expiryTime);

    /**
     * 특정 사용자의 모든 대기열 이력 조회
     */
    List<MatchingQueue> findByUserOrderByJoinedAtDesc(User user);

    /**
     * 특정 시간 범위의 대기열 통계
     */
    @Query("SELECT COUNT(mq) FROM MatchingQueue mq WHERE mq.joinedAt BETWEEN :startTime AND :endTime")
    long countByJoinedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 평균 대기 시간 계산 (분)
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', MINUTE, mq.joinedAt, mq.matchedAt)) " +
           "FROM MatchingQueue mq " +
           "WHERE mq.status = :status " +
           "AND mq.matchedAt IS NOT NULL")
    Double calculateAverageWaitTime(@Param("status") MatchingQueue.QueueStatus status);

    /**
     * 특정 언어의 대기열 항목 조회
     */
    @Query("SELECT mq FROM MatchingQueue mq WHERE mq.status = :status " +
           "AND mq.targetLanguage = :language " +
           "ORDER BY mq.priorityScore DESC, mq.joinedAt ASC")
    List<MatchingQueue> findByStatusAndTargetLanguage(@Param("status") MatchingQueue.QueueStatus status, 
                                                     @Param("language") String language);
}