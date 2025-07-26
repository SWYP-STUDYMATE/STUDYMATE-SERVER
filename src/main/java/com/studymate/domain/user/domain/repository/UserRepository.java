package com.studymate.domain.user.domain.repository;

import com.studymate.domain.user.entity.User;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserIdentity(String identity);
    UUID userId(UUID userId);

    @Query("select u.name from User u where u.userId = :userId")
    String findNameByUserId(@Param("userId") UUID userId);
}
