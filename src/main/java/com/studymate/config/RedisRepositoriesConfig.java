package com.studymate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.studymate.redis.repository")
public class RedisRepositoriesConfig {
}
