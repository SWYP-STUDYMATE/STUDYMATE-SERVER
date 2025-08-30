package com.studymate.domain.session.domain.repository;

import com.studymate.domain.session.entity.Session;
import com.studymate.domain.session.entity.SessionBooking;
import com.studymate.domain.session.type.BookingStatus;
import com.studymate.domain.user.entity.User;
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
public interface SessionBookingRepository extends JpaRepository<SessionBooking, Long> {

    // 세션별 예약 조회
    List<SessionBooking> findBySessionOrderByCreatedAtDesc(Session session);

    // 사용자별 예약 조회
    Page<SessionBooking> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 세션과 사용자로 예약 조회
    Optional<SessionBooking> findBySessionAndUser(Session session, User user);

    // 상태별 예약 조회
    @Query("SELECT sb FROM SessionBooking sb WHERE sb.user.userId = :userId AND sb.status = :status ORDER BY sb.createdAt DESC")
    Page<SessionBooking> findByUserIdAndStatus(@Param("userId") UUID userId, 
                                             @Param("status") BookingStatus status, 
                                             Pageable pageable);

    // 특정 날짜의 사용자 예약 조회
    @Query("SELECT sb FROM SessionBooking sb WHERE sb.user.userId = :userId " +
           "AND DATE(sb.session.scheduledAt) = DATE(:date) ORDER BY sb.session.scheduledAt ASC")
    List<SessionBooking> findUserBookingsByDate(@Param("userId") UUID userId, @Param("date") LocalDateTime date);

    // 곧 시작될 예약들 (알림용)
    @Query("SELECT sb FROM SessionBooking sb WHERE sb.status = :status " +
           "AND sb.session.scheduledAt BETWEEN :now AND :reminderTime AND sb.reminderSent = false")
    List<SessionBooking> findUpcomingBookingsForReminder(@Param("status") BookingStatus status,
                                                        @Param("now") LocalDateTime now,
                                                        @Param("reminderTime") LocalDateTime reminderTime);

    // 세션의 예약 수 조회
    @Query("SELECT COUNT(sb) FROM SessionBooking sb WHERE sb.session.sessionId = :sessionId AND sb.status = :status")
    Long countBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") BookingStatus status);

    // 사용자의 총 예약 수 조회
    @Query("SELECT COUNT(sb) FROM SessionBooking sb WHERE sb.user.userId = :userId AND sb.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") BookingStatus status);

    // 사용자의 출석률 계산용
    @Query("SELECT COUNT(sb) FROM SessionBooking sb WHERE sb.user.userId = :userId AND sb.attended = true")
    Long countAttendedSessionsByUserId(@Param("userId") UUID userId);

    // 피드백이 있는 예약 조회
    @Query("SELECT sb FROM SessionBooking sb WHERE sb.user.userId = :userId AND sb.feedbackRating IS NOT NULL ORDER BY sb.createdAt DESC")
    List<SessionBooking> findBookingsWithFeedbackByUserId(@Param("userId") UUID userId);

    // 특정 기간의 예약 통계
    @Query("SELECT sb.status, COUNT(sb) FROM SessionBooking sb " +
           "WHERE sb.createdAt BETWEEN :startDate AND :endDate GROUP BY sb.status")
    List<Object[]> getBookingStatsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    // 사용자의 이번 주 예약 조회
    @Query("SELECT sb FROM SessionBooking sb WHERE sb.user.userId = :userId " +
           "AND sb.session.scheduledAt BETWEEN :weekStart AND :weekEnd " +
           "AND sb.status = :status ORDER BY sb.session.scheduledAt ASC")
    List<SessionBooking> findUserBookingsThisWeek(@Param("userId") UUID userId,
                                                 @Param("weekStart") LocalDateTime weekStart,
                                                 @Param("weekEnd") LocalDateTime weekEnd,
                                                 @Param("status") BookingStatus status);

    // 중복 예약 체크
    @Query("SELECT COUNT(sb) > 0 FROM SessionBooking sb WHERE sb.user.userId = :userId " +
           "AND sb.session.sessionId = :sessionId AND sb.status = :status")
    Boolean existsByUserIdAndSessionIdAndStatus(@Param("userId") UUID userId,
                                               @Param("sessionId") Long sessionId,
                                               @Param("status") BookingStatus status);
}