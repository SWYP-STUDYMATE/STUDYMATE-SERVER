package com.studymate.domain.user.domain.dao;

import com.studymate.domain.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    Optional<User> findByUserIdentity(String identity);
    Optional <User> findByUserId(UUID userId);
    User save (User user);
}
