package com.studymate.domain.user.domain.repository;

import com.studymate.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, UUID> {

    Optional<UserStatus> findByUserId(UUID userId);

    @Query("SELECT us FROM UserStatus us WHERE us.status = 'ONLINE' OR us.status = 'STUDYING'")
    List<UserStatus> findAllOnlineUsers();

    @Query("SELECT us FROM UserStatus us WHERE us.status = 'STUDYING'")
    List<UserStatus> findAllStudyingUsers();

    @Query("SELECT us FROM UserStatus us WHERE us.userId IN :userIds")
    List<UserStatus> findByUserIds(@Param("userIds") List<UUID> userIds);

    @Modifying
    @Query("UPDATE UserStatus us SET us.status = 'OFFLINE', us.lastSeenAt = :timestamp WHERE us.lastSeenAt < :cutoffTime AND us.status != 'OFFLINE'")
    int markInactiveUsersAsOffline(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT COUNT(us) FROM UserStatus us WHERE us.status = 'ONLINE' OR us.status = 'STUDYING'")
    long countOnlineUsers();

    @Query("SELECT us FROM UserStatus us WHERE us.user.location.city = :city AND (us.status = 'ONLINE' OR us.status = 'STUDYING')")
    List<UserStatus> findOnlineUsersByCity(@Param("city") String city);
}