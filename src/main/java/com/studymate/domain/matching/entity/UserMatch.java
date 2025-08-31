package com.studymate.domain.matching.entity;

import com.studymate.common.entity.BaseTimeEntity;
import com.studymate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 매칭된 사용자 관계 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "USER_MATCH")
public class UserMatch extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "USER_MATCH_ID")
    private UUID userMatchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER1_ID", nullable = false)
    private User user1; // 첫 번째 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER2_ID", nullable = false)
    private User user2; // 두 번째 사용자

    @Column(name = "MATCHED_AT")
    private LocalDateTime matchedAt; // 매칭된 시간

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive; // 매칭 활성 상태

    @Column(name = "DEACTIVATED_AT")
    private LocalDateTime deactivatedAt; // 매칭 해제 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEACTIVATED_BY")
    private User deactivatedBy; // 매칭을 해제한 사용자

    /**
     * 매칭 해제
     */
    public void deactivate(User deactivatedBy) {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
        this.deactivatedBy = deactivatedBy;
    }

    /**
     * 매칭된 상대방 사용자 반환
     */
    public User getPartner(User currentUser) {
        if (currentUser.equals(user1)) {
            return user2;
        } else if (currentUser.equals(user2)) {
            return user1;
        }
        return null;
    }

    /**
     * 사용자가 이 매칭에 포함되는지 확인
     */
    public boolean includesUser(User user) {
        return user.equals(user1) || user.equals(user2);
    }

    // 편의 메서드
    public UUID getMatchId() {
        return this.userMatchId;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }
}