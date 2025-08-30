package com.studymate.domain.matching.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 매칭 요청 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "MATCHING_REQUEST")
public class MatchingRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MATCHING_REQUEST_ID")
    private UUID matchingRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID", nullable = false)
    private User sender; // 매칭 요청을 보낸 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_ID", nullable = false)
    private User receiver; // 매칭 요청을 받는 사용자

    @Column(name = "MESSAGE", length = 500)
    private String message; // 매칭 요청 메시지

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private MatchingStatus status;

    @Column(name = "RESPONSE_MESSAGE", length = 500)
    private String responseMessage; // 수락/거절 시 메시지

    @Column(name = "RESPONDED_AT")
    private LocalDateTime respondedAt; // 응답한 시간

    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt; // 요청 만료 시간

    /**
     * 매칭 요청 수락
     */
    public void accept(String responseMessage) {
        this.status = MatchingStatus.ACCEPTED;
        this.responseMessage = responseMessage;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * 매칭 요청 거절
     */
    public void reject(String responseMessage) {
        this.status = MatchingStatus.REJECTED;
        this.responseMessage = responseMessage;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * 매칭 요청 취소
     */
    public void cancel() {
        this.status = MatchingStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * 요청 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // 편의 메서드들
    public UUID getRequestId() {
        return this.matchingRequestId;
    }

    public LocalDateTime getResponseAt() {
        return this.respondedAt;
    }
}