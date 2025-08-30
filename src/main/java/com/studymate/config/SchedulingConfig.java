package com.studymate.config;

import com.studymate.domain.user.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final UserStatusService userStatusService;

    /**
     * 5분마다 비활성 사용자들을 오프라인으로 처리
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void cleanupInactiveUsers() {
        log.debug("Starting cleanup of inactive users");
        try {
            userStatusService.cleanupInactiveUsers();
        } catch (Exception e) {
            log.error("Error during inactive users cleanup: ", e);
        }
    }
}