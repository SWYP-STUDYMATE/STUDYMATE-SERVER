package com.studymate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "redis.host")
public class RedisConfig {

    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;
    @Value("${redis.password:}")
    private String redisPassword;

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== Redis Configuration Debug ===");
        
        if (redisHost == null || redisHost.trim().isEmpty()) {
            log.error("❌ CRITICAL: redis.host is NULL or EMPTY! Current value: '{}'", redisHost);
        } else {
            log.info("✅ redis.host: {}", redisHost);
        }
        
        if (redisPort <= 0) {
            log.error("❌ CRITICAL: redis.port is invalid! Current value: {}", redisPort);
        } else {
            log.info("✅ redis.port: {}", redisPort);
        }
        
        if (redisPassword == null) {
            log.warn("⚠️  redis.password is NULL (no password auth)");
        } else if (redisPassword.trim().isEmpty()) {
            log.warn("⚠️  redis.password is EMPTY (no password auth)");
        } else {
            log.info("✅ redis.password: *** (masked)");
        }
        
        log.info("=== Redis Configuration Debug Complete ===");
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String,String> redisTemplate(){
        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }



}
