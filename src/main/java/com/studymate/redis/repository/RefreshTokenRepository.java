package com.studymate.redis.repository;

import org.springframework.data.repository.CrudRepository;

import com.studymate.redis.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
