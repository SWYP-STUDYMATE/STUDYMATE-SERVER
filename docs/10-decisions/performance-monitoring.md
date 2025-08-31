# Performance Monitoring & Observability

## 1. 개요

STUDYMATE 플랫폼의 성능 모니터링, 로깅, 알림 시스템에 대한 가이드입니다. 
시스템의 안정성, 성능 최적화, 장애 대응을 위한 종합적인 관찰 가능성(Observability) 전략을 제시합니다.

## 2. 모니터링 아키텍처

### 2.1 모니터링 스택

```yaml
Observability Stack:
  Metrics:
    - Micrometer (Spring Boot Actuator)
    - Prometheus (메트릭 수집 및 저장)
    - Grafana (시각화 대시보드)
  
  Logging:
    - Logback (구조화된 JSON 로깅)
    - ELK Stack (Elasticsearch + Logstash + Kibana)
    - NCP Log Analytics
  
  Tracing:
    - Spring Cloud Sleuth
    - Zipkin (분산 추적)
  
  APM:
    - New Relic / Datadog (선택사항)
    - Spring Boot Admin (경량 모니터링)

Infrastructure:
  - NCP Cloud Insight
  - Load Balancer 헬스체크
  - Database 성능 모니터링
```

### 2.2 메트릭 수집 설정

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
      version: ${BUILD_VERSION:unknown}
```

```java
// 커스텀 메트릭 등록
@Component
public class CustomMetrics {
    private final Counter userRegistrations;
    private final Timer chatMessageProcessing;
    private final Gauge activeUsers;
    private final MeterRegistry meterRegistry;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userRegistrations = Counter.builder("user.registrations.total")
                .description("Total number of user registrations")
                .register(meterRegistry);
        
        this.chatMessageProcessing = Timer.builder("chat.message.processing.time")
                .description("Chat message processing time")
                .register(meterRegistry);
        
        this.activeUsers = Gauge.builder("users.active.count")
                .description("Number of currently active users")
                .register(meterRegistry, this, CustomMetrics::getActiveUserCount);
    }

    public void recordUserRegistration() {
        userRegistrations.increment();
    }

    public Timer.Sample startMessageProcessing() {
        return Timer.start(meterRegistry);
    }

    private double getActiveUserCount() {
        // Redis에서 활성 사용자 수 조회
        return redisTemplate.opsForSet().size("active_users");
    }
}
```

## 3. 핵심 성능 지표 (KPI)

### 3.1 비즈니스 메트릭

```yaml
User Metrics:
  - user.registrations.total: 총 사용자 등록 수
  - user.login.success.rate: 로그인 성공률
  - user.retention.rate: 사용자 리텐션율 (일/주/월)
  - user.session.duration: 평균 세션 시간

Learning Metrics:
  - matching.success.rate: 매칭 성공률
  - matching.response.time: 매칭 처리 시간
  - chat.messages.per.session: 세션당 메시지 수
  - level.test.completion.rate: 레벨 테스트 완료율

Technical Metrics:
  - api.response.time: API 응답 시간
  - websocket.connections.active: 활성 WebSocket 연결 수
  - database.query.performance: 데이터베이스 쿼리 성능
  - cache.hit.ratio: 캐시 히트율
```

### 3.2 시스템 메트릭

```yaml
Application Metrics:
  - jvm.memory.used: JVM 메모리 사용량
  - jvm.gc.pause: GC 일시정지 시간
  - http.server.requests: HTTP 요청 메트릭
  - database.connections.active: DB 연결 풀 사용량

Infrastructure Metrics:
  - system.cpu.usage: CPU 사용률
  - system.memory.usage: 메모리 사용률
  - disk.usage: 디스크 사용량
  - network.bytes.transmitted: 네트워크 전송량
```

## 4. 구조화된 로깅

### 4.1 로깅 설정

```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <springProfile name="!prod">
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <!-- JSON 구조화 로깅 -->
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeContext>true</includeContext>
                <includeMdc>true</includeMdc>
                <customFields>{"service":"studymate-server"}</customFields>
                <fieldNames>
                    <timestamp>@timestamp</timestamp>
                    <level>level</level>
                    <message>message</message>
                    <logger>logger</logger>
                    <thread>thread</thread>
                </fieldNames>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>
    
    <!-- 보안 로그 -->
    <logger name="SECURITY" level="INFO" additivity="false">
        <appender-ref ref="JSON"/>
    </logger>
    
    <!-- 성능 로그 -->
    <logger name="PERFORMANCE" level="DEBUG" additivity="false">
        <appender-ref ref="JSON"/>
    </logger>
</configuration>
```

### 4.2 로깅 전략

```java
// 구조화된 로깅 유틸리티
@Component
public class StructuredLogger {
    private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY");
    private static final Logger performanceLog = LoggerFactory.getLogger("PERFORMANCE");

    public void logUserAction(String action, String userId, Map<String, Object> context) {
        MDC.put("userId", userId);
        MDC.put("action", action);
        
        try {
            log.info("User action executed: {}", 
                buildLogMessage(action, context));
        } finally {
            MDC.clear();
        }
    }

    public void logSecurityEvent(SecurityEvent event) {
        MDC.put("eventType", event.getType().name());
        MDC.put("userId", event.getUserId());
        MDC.put("ipAddress", event.getIpAddress());
        
        try {
            securityLog.warn("Security event: {}", 
                objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to log security event", e);
        } finally {
            MDC.clear();
        }
    }

    public void logPerformance(String operation, long duration, Map<String, Object> metadata) {
        MDC.put("operation", operation);
        MDC.put("duration", String.valueOf(duration));
        
        try {
            performanceLog.info("Performance metric: {} took {}ms", 
                operation, duration, metadata);
        } finally {
            MDC.clear();
        }
    }
}

// AOP를 활용한 자동 성능 로깅
@Aspect
@Component
public class PerformanceLoggingAspect {
    private final StructuredLogger structuredLogger;

    @Around("@annotation(MonitorPerformance)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            structuredLogger.logPerformance(
                joinPoint.getSignature().toShortString(),
                duration,
                Map.of("success", true)
            );
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            structuredLogger.logPerformance(
                joinPoint.getSignature().toShortString(),
                duration,
                Map.of("success", false, "error", e.getMessage())
            );
            
            throw e;
        }
    }
}
```

## 5. 알림 및 경고 시스템

### 5.1 알림 규칙 정의

```yaml
# Prometheus 알림 규칙 (alerts.yml)
groups:
  - name: studymate-alerts
    rules:
      # 높은 오류율
      - alert: HighErrorRate
        expr: rate(http_server_requests_total{status=~"5.."}[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for more than 2 minutes"

      # 높은 응답 시간
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High API response time"
          description: "95th percentile response time is above 2 seconds"

      # 데이터베이스 연결 부족
      - alert: DatabaseConnectionsLow
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool usage high"
          description: "Database connection pool is more than 80% utilized"

      # 메모리 사용량 높음
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High JVM heap memory usage"
          description: "JVM heap memory usage is above 80%"

      # WebSocket 연결 이상
      - alert: WebSocketConnectionsDrop
        expr: websocket_connections_active < 10 and websocket_connections_active[1h] > 100
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "WebSocket connections dropped significantly"
          description: "WebSocket connections dropped from >100 to <10"
```

### 5.2 Slack 알림 통합

```java
@Component
public class AlertingService {
    private final WebClient webClient;
    
    @Value("${alerting.slack.webhook-url}")
    private String slackWebhookUrl;

    public void sendSlackAlert(AlertLevel level, String title, String message, Map<String, String> context) {
        SlackMessage slackMessage = SlackMessage.builder()
                .text(title)
                .attachments(List.of(
                    SlackAttachment.builder()
                        .color(getColorForLevel(level))
                        .title(title)
                        .text(message)
                        .fields(buildContextFields(context))
                        .timestamp(Instant.now().getEpochSecond())
                        .build()
                ))
                .build();

        webClient.post()
                .uri(slackWebhookUrl)
                .bodyValue(slackMessage)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                    response -> log.info("Alert sent to Slack: {}", title),
                    error -> log.error("Failed to send Slack alert", error)
                );
    }

    private String getColorForLevel(AlertLevel level) {
        return switch (level) {
            case CRITICAL -> "#ff0000";  // Red
            case WARNING -> "#ffaa00";   // Orange
            case INFO -> "#00aa00";      // Green
        };
    }
}

// 이벤트 기반 알림
@Component
public class AlertEventHandler {
    private final AlertingService alertingService;

    @EventListener
    public void handleUserRegistrationSpike(UserRegistrationSpikeEvent event) {
        alertingService.sendSlackAlert(
            AlertLevel.INFO,
            "User Registration Spike Detected",
            String.format("Registration rate: %d/hour (normal: %d/hour)", 
                event.getCurrentRate(), event.getNormalRate()),
            Map.of(
                "period", event.getPeriod(),
                "threshold", String.valueOf(event.getThreshold())
            )
        );
    }

    @EventListener
    public void handleDatabaseSlowQuery(DatabaseSlowQueryEvent event) {
        alertingService.sendSlackAlert(
            AlertLevel.WARNING,
            "Slow Database Query Detected",
            String.format("Query took %dms: %s", event.getDuration(), event.getQuery()),
            Map.of(
                "table", event.getTable(),
                "operation", event.getOperation(),
                "threshold", "1000ms"
            )
        );
    }
}
```

## 6. 대시보드 설계

### 6.1 Grafana 대시보드 구성

```json
{
  "dashboard": {
    "title": "STUDYMATE Application Dashboard",
    "panels": [
      {
        "title": "Request Rate & Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_total[5m])",
            "legendFormat": "Request Rate (/s)"
          },
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket[5m]))",
            "legendFormat": "95th Percentile Response Time"
          }
        ]
      },
      {
        "title": "Active Users & Sessions",
        "type": "stat",
        "targets": [
          {
            "expr": "users_active_count",
            "legendFormat": "Active Users"
          },
          {
            "expr": "websocket_connections_active",
            "legendFormat": "WebSocket Connections"
          }
        ]
      },
      {
        "title": "Database Performance",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active",
            "legendFormat": "Active DB Connections"
          },
          {
            "expr": "rate(database_queries_total[5m])",
            "legendFormat": "Queries/sec"
          }
        ]
      },
      {
        "title": "Error Rate by Endpoint",
        "type": "table",
        "targets": [
          {
            "expr": "rate(http_server_requests_total{status=~\"4..|5..\"}[5m]) by (uri, status)",
            "format": "table"
          }
        ]
      }
    ]
  }
}
```

### 6.2 비즈니스 메트릭 대시보드

```yaml
Business Metrics Dashboard:
  Top Row:
    - Daily Active Users (DAU)
    - New User Registrations
    - User Retention Rate
    - Average Session Duration

  Middle Row:
    - Matching Success Rate
    - Messages per Session
    - Level Test Completion Rate
    - Achievement Unlock Rate

  Bottom Row:
    - Revenue Metrics (if applicable)
    - Feature Usage Heatmap
    - Geographic User Distribution
    - Language Learning Progress
```

## 7. 성능 최적화 모니터링

### 7.1 데이터베이스 쿼리 모니터링

```java
// 슬로우 쿼리 감지 및 로깅
@Component
public class DatabasePerformanceMonitor {
    private final Timer.Sample currentSample = Timer.start();
    
    @EventListener
    public void handleSlowQuery(SlowQueryEvent event) {
        if (event.getDuration() > 1000) { // 1초 이상
            structuredLogger.logPerformance(
                "slow_query",
                event.getDuration(),
                Map.of(
                    "query", event.getQuery(),
                    "table", event.getTable(),
                    "rows_examined", event.getRowsExamined(),
                    "rows_sent", event.getRowsSent()
                )
            );
            
            // 크리티컬한 슬로우 쿼리는 즉시 알림
            if (event.getDuration() > 5000) {
                alertingService.sendSlackAlert(
                    AlertLevel.CRITICAL,
                    "Critical Slow Query",
                    String.format("Query took %dms", event.getDuration()),
                    Map.of("query", event.getQuery().substring(0, 100) + "...")
                );
            }
        }
    }
}

// JPA 쿼리 성능 모니터링
@Configuration
public class JpaPerformanceConfig {
    
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (properties) -> {
            properties.put("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", 1000);
            properties.put("hibernate.generate_statistics", true);
        };
    }
}
```

### 7.2 캐시 성능 모니터링

```java
// Redis 캐시 성능 모니터링
@Component
public class CacheMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter cacheHit;
    private final Counter cacheMiss;
    private final Timer cacheOperationTime;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.cacheHit = Counter.builder("cache.operations")
                .tag("result", "hit")
                .register(meterRegistry);
        this.cacheMiss = Counter.builder("cache.operations")
                .tag("result", "miss")
                .register(meterRegistry);
        this.cacheOperationTime = Timer.builder("cache.operation.time")
                .register(meterRegistry);
    }

    public void recordCacheHit() {
        cacheHit.increment();
    }

    public void recordCacheMiss() {
        cacheMiss.increment();
    }

    // 캐시 히트율 계산
    @Scheduled(fixedRate = 60000) // 1분마다
    public void calculateCacheHitRatio() {
        double totalOperations = cacheHit.count() + cacheMiss.count();
        double hitRatio = totalOperations > 0 ? cacheHit.count() / totalOperations : 0;
        
        Gauge.builder("cache.hit.ratio")
                .register(meterRegistry, () -> hitRatio);
        
        // 히트율이 너무 낮으면 알림
        if (hitRatio < 0.7 && totalOperations > 100) {
            alertingService.sendSlackAlert(
                AlertLevel.WARNING,
                "Low Cache Hit Ratio",
                String.format("Cache hit ratio is %.2f%% (expected >70%%)", hitRatio * 100),
                Map.of("hit_count", String.valueOf(cacheHit.count()),
                      "miss_count", String.valueOf(cacheMiss.count()))
            );
        }
    }
}
```

## 8. 보안 모니터링

### 8.1 보안 이벤트 추적

```java
// 보안 이벤트 모니터링
@Component
public class SecurityMonitoring {
    private final Counter failedLoginAttempts;
    private final Counter suspiciousActivity;

    public SecurityMonitoring(MeterRegistry meterRegistry) {
        this.failedLoginAttempts = Counter.builder("security.login.failed")
                .register(meterRegistry);
        this.suspiciousActivity = Counter.builder("security.suspicious.activity")
                .register(meterRegistry);
    }

    @EventListener
    public void handleFailedLogin(AuthenticationFailureEvent event) {
        failedLoginAttempts.increment(
            Tags.of(
                "reason", event.getException().getClass().getSimpleName(),
                "username", maskUsername(event.getAuthentication().getName())
            )
        );

        // 연속 실패 시 알림
        String clientIp = getClientIp(event);
        if (countRecentFailures(clientIp) > 5) {
            alertingService.sendSlackAlert(
                AlertLevel.WARNING,
                "Suspicious Login Activity",
                String.format("Multiple failed login attempts from IP: %s", clientIp),
                Map.of("ip", clientIp, "attempts", "5+")
            );
        }
    }

    @EventListener
    public void handleSuspiciousActivity(SuspiciousActivityEvent event) {
        suspiciousActivity.increment(
            Tags.of("type", event.getActivityType())
        );

        structuredLogger.logSecurityEvent(
            SecurityEvent.builder()
                .type(SecurityEventType.SUSPICIOUS_ACTIVITY)
                .userId(event.getUserId())
                .ipAddress(event.getIpAddress())
                .description(event.getDescription())
                .timestamp(Instant.now())
                .build()
        );
    }
}
```

### 8.2 API 사용량 모니터링

```java
// API 레이트 리미팅 모니터링
@Component
public class RateLimitMonitoring {
    private final Counter rateLimitExceeded;

    @EventListener
    public void handleRateLimitExceeded(RateLimitExceededEvent event) {
        rateLimitExceeded.increment(
            Tags.of(
                "endpoint", event.getEndpoint(),
                "user_id", event.getUserId()
            )
        );

        // 반복적인 레이트 리미트 위반 시 알림
        if (event.getViolationCount() > 10) {
            alertingService.sendSlackAlert(
                AlertLevel.WARNING,
                "Repeated Rate Limit Violations",
                String.format("User %s exceeded rate limit %d times for %s", 
                    event.getUserId(), event.getViolationCount(), event.getEndpoint()),
                Map.of(
                    "user_id", event.getUserId(),
                    "endpoint", event.getEndpoint(),
                    "violations", String.valueOf(event.getViolationCount())
                )
            );
        }
    }
}
```

## 9. 헬스체크 및 가용성 모니터링

### 9.1 종합 헬스체크

```java
// 커스텀 헬스 인디케이터
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "MySQL connection successful")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "MySQL connection failed")
                    .withException(e)
                    .build();
        }
    }
}

@Component
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Health health() {
        try {
            redisTemplate.opsForValue().set("health-check", "ok", Duration.ofSeconds(10));
            String value = redisTemplate.opsForValue().get("health-check");
            
            if ("ok".equals(value)) {
                return Health.up()
                        .withDetail("redis", "Connection successful")
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "Unexpected response")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "Connection failed")
                    .withException(e)
                    .build();
        }
    }
}
```

### 9.2 외부 의존성 모니터링

```java
// 외부 API 헬스체크
@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {
    private final WebClient naverApiClient;
    private final WebClient clovaStudioClient;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // Naver API 상태 확인
        try {
            // Simple ping 또는 가벼운 API 호출
            naverApiClient.get()
                    .uri("/ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            builder.withDetail("naver-api", "Available");
        } catch (Exception e) {
            builder.down().withDetail("naver-api", "Unavailable: " + e.getMessage());
        }

        // Clova Studio API 상태 확인
        try {
            // Health check endpoint 호출
            clovaStudioClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            builder.withDetail("clova-studio", "Available");
        } catch (Exception e) {
            builder.withDetail("clova-studio", "Unavailable: " + e.getMessage());
        }

        return builder.build();
    }
}
```

## 10. 운영 자동화

### 10.1 자동 스케일링 트리거

```yaml
# NCP Auto Scaling 정책
auto_scaling_policies:
  scale_out:
    metric: cpu_utilization
    threshold: 70
    duration: 300  # 5분
    action: add_instance
    cooldown: 600  # 10분

  scale_in:
    metric: cpu_utilization
    threshold: 30
    duration: 600  # 10분
    action: remove_instance
    cooldown: 300  # 5분

  memory_based:
    metric: memory_utilization
    threshold: 80
    duration: 300
    action: add_instance
```

### 10.2 자동 복구 스크립트

```bash
#!/bin/bash
# auto-recovery.sh

# 헬스체크 실패 시 자동 복구
check_health() {
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$response" != "200" ]; then
        echo "Health check failed with status: $response"
        return 1
    fi
    return 0
}

# 애플리케이션 재시작
restart_application() {
    echo "Restarting application..."
    systemctl restart studymate-server
    sleep 30
    
    if check_health; then
        echo "Application restart successful"
        # Slack 알림
        curl -X POST -H 'Content-type: application/json' \
            --data '{"text":"🔄 STUDYMATE Server automatically restarted and is healthy"}' \
            $SLACK_WEBHOOK_URL
    else
        echo "Application restart failed"
        # 긴급 알림
        curl -X POST -H 'Content-type: application/json' \
            --data '{"text":"🚨 CRITICAL: STUDYMATE Server restart failed - manual intervention required"}' \
            $SLACK_WEBHOOK_URL
    fi
}

# 메인 복구 로직
if ! check_health; then
    restart_application
fi
```

## 11. 성능 기준선 및 SLA

### 11.1 성능 목표

```yaml
Performance Targets:
  API Response Time:
    - 95th percentile: < 500ms
    - 99th percentile: < 1000ms
    - Maximum: < 5000ms

  Database Queries:
    - Average: < 100ms
    - 95th percentile: < 500ms
    - Slow query threshold: > 1000ms

  WebSocket:
    - Connection establishment: < 1000ms
    - Message delivery: < 100ms
    - Max concurrent connections: 10,000

  System Resources:
    - CPU utilization: < 70% (normal)
    - Memory utilization: < 80%
    - Disk usage: < 85%
    - Network latency: < 50ms
```

### 11.2 SLA 정의

```yaml
Service Level Agreement:
  Availability:
    - Target: 99.5% uptime
    - Maximum downtime: 3.6 hours/month
    - Planned maintenance window: Weekly 2AM-4AM KST

  Performance:
    - API response time: 95% under 500ms
    - Error rate: < 1%
    - Recovery time: < 5 minutes

  Data:
    - Backup frequency: Daily
    - Retention period: 30 days
    - Recovery point objective (RPO): 24 hours
    - Recovery time objective (RTO): 4 hours
```

이 성능 모니터링 문서는 STUDYMATE 플랫폼의 안정적인 운영과 지속적인 성능 개선을 위한 종합적인 가이드입니다. 실제 운영 환경에서는 이러한 모니터링 시스템을 통해 사용자 경험을 향상시키고 시스템 안정성을 보장할 수 있습니다.