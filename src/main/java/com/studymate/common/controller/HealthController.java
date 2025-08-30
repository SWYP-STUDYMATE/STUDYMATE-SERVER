package com.studymate.common.controller;

import com.studymate.common.dto.ApiResponse;
import com.studymate.common.health.CustomHealthIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController()
@RequiredArgsConstructor
public class HealthController {

    private final CustomHealthIndicator customHealthIndicator;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("application", "STUDYMATE-SERVER");
        healthData.put("version", "1.0.0");
        
        // 커스텀 헬스 체크 수행
        Map<String, Object> customHealth = customHealthIndicator.checkHealth();
        healthData.put("status", customHealth.get("status"));
        healthData.put("database", customHealth.get("database"));
        healthData.put("userCount", customHealth.get("userCount"));
        
        if ("DOWN".equals(customHealth.get("status"))) {
            healthData.put("error", customHealth.get("error"));
            return ResponseEntity.status(503).body(
                ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .data(healthData)
                    .message("Health check failed")
                    .build()
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success(healthData, "Health check completed"));
    }
}
