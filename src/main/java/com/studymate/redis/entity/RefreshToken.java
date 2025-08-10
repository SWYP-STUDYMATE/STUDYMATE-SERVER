package com.studymate.redis.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("refresh_token")
public class RefreshToken {

    @Id
    private String userId;

    private String token;

    @TimeToLive
    private Long ttlSeconds;
}
