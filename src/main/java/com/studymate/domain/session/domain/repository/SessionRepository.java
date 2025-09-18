package com.studymate.domain.session.domain.repository;

import com.studymate.domain.session.entity.Session;
import com.studymate.domain.session.type.SessionStatus;
import com.studymate.domain.session.type.SessionType;
import com.studymate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // 호스트 기준 세션 조회
    Page<Session> findByHostUserOrderByScheduledAtDesc(User hostUser, Pageable pageable);

    // 게스트 기준 세션 조회
    Page<Session> findByGuestUserOrderByScheduledAtDesc(User guestUser, Pageable pageable);

    // 사용자가 참여한 모든 세션 조회
    @Query("SELECT s FROM Session s WHERE s.hostUser.userId = :userId OR s.guestUser.userId = :userId ORDER BY s.scheduledAt DESC")
    Page<Session> findByUserIdOrderByScheduledAtDesc(@Param("userId") UUID userId, Pageable pageable);

    // 공개 세션 목록 조회
    @Query("SELECT s FROM Session s WHERE s.isPublic = true AND s.status = :status AND s.scheduledAt > :now ORDER BY s.scheduledAt ASC")
    Page<Session> findPublicSessionsByStatus(@Param("status") SessionStatus status, 
                                           @Param("now") LocalDateTime now, 
                                           Pageable pageable);

    // 언어별 세션 조회
    @Query("SELECT s FROM Session s WHERE s.languageCode = :languageCode AND s.isPublic = true AND s.status = :status AND s.scheduledAt > :now ORDER BY s.scheduledAt ASC")
    Page<Session> findByLanguageCodeAndStatus(@Param("languageCode") String languageCode, 
                                            @Param("status") SessionStatus status, 
                                            @Param("now") LocalDateTime now, 
                                            Pageable pageable);

    // 세션 타입별 조회
    @Query("SELECT s FROM Session s WHERE s.sessionType = :sessionType AND s.isPublic = true AND s.status = :status AND s.scheduledAt > :now ORDER BY s.scheduledAt ASC")
    Page<Session> findBySessionTypeAndStatus(@Param("sessionType") SessionType sessionType, 
                                           @Param("status") SessionStatus status, 
                                           @Param("now") LocalDateTime now, 
                                           Pageable pageable);

    // 특정 날짜 범위의 세션 조회
    @Query("SELECT s FROM Session s WHERE s.scheduledAt BETWEEN :startDate AND :endDate ORDER BY s.scheduledAt ASC")
    List<Session> findSessionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    // 사용자의 특정 날짜 세션 조회
    @Query("SELECT s FROM Session s WHERE (s.hostUser.userId = :userId OR s.guestUser.userId = :userId) " +
           "AND DATE(s.scheduledAt) = DATE(:date) ORDER BY s.scheduledAt ASC")
    List<Session> findUserSessionsByDate(@Param("userId") UUID userId, @Param("date") LocalDateTime date);

    // 곧 시작될 세션 조회 (알림용)
    @Query("SELECT s FROM Session s WHERE s.status = :status AND s.scheduledAt BETWEEN :now AND :reminderTime")
    List<Session> findUpcomingSessions(@Param("status") SessionStatus status, 
                                     @Param("now") LocalDateTime now, 
                                     @Param("reminderTime") LocalDateTime reminderTime);

    // 반복 세션 조회
    @Query("SELECT s FROM Session s WHERE s.isRecurring = true AND s.hostUser.userId = :userId ORDER BY s.scheduledAt DESC")
    List<Session> findRecurringSessionsByHostUser(@Param("userId") UUID userId);

    // 참여 가능한 세션 조회
    @Query("SELECT s FROM Session s WHERE s.isPublic = true AND s.status = :status " +
           "AND s.scheduledAt > :now AND s.currentParticipants < s.maxParticipants " +
           "AND s.hostUser.userId != :userId ORDER BY s.scheduledAt ASC")
    Page<Session> findAvailableSessionsForUser(@Param("userId") UUID userId, 
                                             @Param("status") SessionStatus status, 
                                             @Param("now") LocalDateTime now, 
                                             Pageable pageable);

    // 태그로 세션 검색
    @Query("SELECT s FROM Session s WHERE s.tags LIKE %:tag% AND s.isPublic = true " +
           "AND s.status = :status AND s.scheduledAt > :now ORDER BY s.scheduledAt ASC")
    Page<Session> findByTagsContainingAndStatus(@Param("tag") String tag, 
                                              @Param("status") SessionStatus status, 
                                              @Param("now") LocalDateTime now, 
                                              Pageable pageable);

    // 사용자별 완료된 세션 수 조회
    @Query("SELECT COUNT(s) FROM Session s WHERE (s.hostUser.userId = :userId OR s.guestUser.userId = :userId) AND s.status = :status")
    Long countCompletedSessionsByUserId(@Param("userId") UUID userId, @Param("status") SessionStatus status);

    // 특정 사용자와 파트너 간 완료된 세션 수 조회
    @Query("SELECT COUNT(s) FROM Session s WHERE ((s.hostUser.userId = :userId AND s.guestUser.userId = :partnerId) " +
           "OR (s.hostUser.userId = :partnerId AND s.guestUser.userId = :userId)) AND s.status = :status")
    Long countCompletedSessionsBetweenUsers(@Param("userId") UUID userId,
                                            @Param("partnerId") UUID partnerId,
                                            @Param("status") SessionStatus status);

    // 이번 주 세션 조회
    @Query("SELECT s FROM Session s WHERE (s.hostUser.userId = :userId OR s.guestUser.userId = :userId) " +
           "AND s.scheduledAt BETWEEN :weekStart AND :weekEnd ORDER BY s.scheduledAt ASC")
    List<Session> findUserSessionsThisWeek(@Param("userId") UUID userId, 
                                         @Param("weekStart") LocalDateTime weekStart, 
                                         @Param("weekEnd") LocalDateTime weekEnd);
}
