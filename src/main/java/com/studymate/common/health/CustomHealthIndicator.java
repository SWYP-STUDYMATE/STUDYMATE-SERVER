package com.studymate.common.health;

import com.studymate.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomHealthIndicator {
    
    private final UserRepository userRepository;
    
    public Map<String, Object> checkHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // 데이터베이스 연결 상태 확인
            long userCount = userRepository.count();
            
            log.debug("Health check - Database connection successful, user count: {}", userCount);
            
            healthStatus.put("status", "UP");
            healthStatus.put("database", "Available");
            healthStatus.put("userCount", userCount);
            healthStatus.put("message", "Healthy");
            
        } catch (Exception e) {
            log.error("Health check failed - Database connection error: {}", e.getMessage());
            
            healthStatus.put("status", "DOWN");
            healthStatus.put("database", "Unavailable");
            healthStatus.put("error", e.getMessage());
            healthStatus.put("message", "Unhealthy");
        }
        
        return healthStatus;
    }
}