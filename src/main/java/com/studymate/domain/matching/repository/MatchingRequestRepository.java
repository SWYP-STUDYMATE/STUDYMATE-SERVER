package com.studymate.domain.matching.repository;

import com.studymate.domain.matching.entity.MatchingRequest;
import com.studymate.domain.matching.entity.MatchingStatus;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest, UUID> {

    /**
     * 받은 매칭 요청 목록 조회 (상태별)
     */
    List<MatchingRequest> findByReceiverAndStatusOrderByCreatedAtDesc(User receiver, MatchingStatus status);

    /**
     * 보낸 매칭 요청 목록 조회 (상태별)
     */
    List<MatchingRequest> findBySenderAndStatusOrderByCreatedAtDesc(User sender, MatchingStatus status);

    /**
     * 특정 사용자가 받은 모든 매칭 요청 (최신순)
     */
    List<MatchingRequest> findByReceiverOrderByCreatedAtDesc(User receiver);

    /**
     * 특정 사용자가 보낸 모든 매칭 요청 (최신순)
     */
    List<MatchingRequest> findBySenderOrderByCreatedAtDesc(User sender);

    /**
     * 두 사용자 간의 최근 매칭 요청 확인
     */
    @Query("SELECT mr FROM MatchingRequest mr WHERE " +
           "((mr.sender = :user1 AND mr.receiver = :user2) OR " +
           "(mr.sender = :user2 AND mr.receiver = :user1)) AND " +
           "mr.status = :status " +
           "ORDER BY mr.createdAt DESC")
    Optional<MatchingRequest> findLatestRequestBetweenUsers(
            @Param("user1") User user1, 
            @Param("user2") User user2, 
            @Param("status") MatchingStatus status);

    /**
     * 만료된 매칭 요청들 조회
     */
    @Query("SELECT mr FROM MatchingRequest mr WHERE " +
           "mr.status = :status AND mr.expiresAt < :currentTime")
    List<MatchingRequest> findExpiredRequests(
            @Param("status") MatchingStatus status, 
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * 사용자가 특정 상대에게 보낸 대기중인 요청이 있는지 확인
     */
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, MatchingStatus status);

    /**
     * 사용자의 받은 대기중인 요청 개수
     */
    long countByReceiverAndStatus(User receiver, MatchingStatus status);
}