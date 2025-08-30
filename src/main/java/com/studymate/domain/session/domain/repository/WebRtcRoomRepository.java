package com.studymate.domain.session.domain.repository;

import com.studymate.domain.session.entity.WebRtcRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebRtcRoomRepository extends JpaRepository<WebRtcRoom, Long> {

    /**
     * 룸 ID로 WebRTC 룸 조회
     */
    Optional<WebRtcRoom> findByRoomId(UUID roomId);

    /**
     * 세션 ID로 WebRTC 룸 조회
     */
    Optional<WebRtcRoom> findBySessionSessionId(Long sessionId);

    /**
     * 활성 상태인 WebRTC 룸들 조회
     */
    @Query("SELECT w FROM WebRtcRoom w WHERE w.status = 'ACTIVE'")
    List<WebRtcRoom> findActiveRooms();

    /**
     * 특정 상태의 WebRTC 룸들 조회
     */
    List<WebRtcRoom> findByStatus(WebRtcRoom.WebRtcRoomStatus status);

    /**
     * 특정 시간 이후에 생성된 WebRTC 룸들 조회
     */
    @Query("SELECT w FROM WebRtcRoom w WHERE w.createdAt >= :startTime")
    List<WebRtcRoom> findByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * 특정 시간 범위 내의 WebRTC 룸들 조회
     */
    @Query("SELECT w FROM WebRtcRoom w WHERE w.createdAt BETWEEN :startTime AND :endTime")
    List<WebRtcRoom> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 현재 참가자 수가 최대 참가자 수보다 적은 활성 룸들 조회
     */
    @Query("SELECT w FROM WebRtcRoom w WHERE w.status = 'ACTIVE' AND w.currentParticipants < w.maxParticipants")
    List<WebRtcRoom> findAvailableActiveRooms();

    /**
     * 녹화가 활성화된 WebRTC 룸들 조회
     */
    List<WebRtcRoom> findByIsRecordingEnabledTrue();

    /**
     * 오래된 종료된 룸들 조회 (정리용)
     */
    @Query("SELECT w FROM WebRtcRoom w WHERE w.status = 'ENDED' AND w.endedAt < :cutoffTime")
    List<WebRtcRoom> findOldEndedRooms(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 특정 기간 동안의 활성 룸 통계
     */
    @Query("SELECT COUNT(w) FROM WebRtcRoom w WHERE w.status = 'ACTIVE' AND w.startedAt BETWEEN :startTime AND :endTime")
    Long countActiveRoomsBetween(@Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 기간 동안의 평균 세션 시간 (분)
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', MINUTE, w.startedAt, w.endedAt)) " +
           "FROM WebRtcRoom w " +
           "WHERE w.status = 'ENDED' AND w.startedAt BETWEEN :startTime AND :endTime " +
           "AND w.endedAt IS NOT NULL")
    Double getAverageSessionDurationMinutes(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 기간 동안의 총 참가자 수
     */
    @Query("SELECT SUM(w.currentParticipants) FROM WebRtcRoom w WHERE w.startedAt BETWEEN :startTime AND :endTime")
    Long getTotalParticipantsBetween(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 시간별 활성 룸 수 통계
     */
    @Query("SELECT HOUR(w.startedAt) as hour, COUNT(w) as count " +
           "FROM WebRtcRoom w " +
           "WHERE w.startedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY HOUR(w.startedAt) " +
           "ORDER BY hour")
    List<Object[]> getHourlyRoomStatistics(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 룸 상태별 통계
     */
    @Query("SELECT w.status, COUNT(w) FROM WebRtcRoom w GROUP BY w.status")
    List<Object[]> getRoomStatusStatistics();

    /**
     * 참가자 수별 룸 분포
     */
    @Query("SELECT w.currentParticipants, COUNT(w) FROM WebRtcRoom w WHERE w.status = 'ACTIVE' GROUP BY w.currentParticipants")
    List<Object[]> getParticipantDistribution();
}