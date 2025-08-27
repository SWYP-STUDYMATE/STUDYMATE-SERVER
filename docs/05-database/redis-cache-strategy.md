# 🚀 Redis 캐시 전략

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Backend Development Team
- **목적**: Redis를 활용한 캐싱 전략 및 성능 최적화

---

## 🎯 캐시 전략 개요

### 캐시 사용 목적
1. **성능 향상**: 데이터베이스 부하 감소 및 응답 속도 개선
2. **확장성**: 높은 동시 접속자 수 지원
3. **실시간성**: 채팅, WebRTC 시그널링 등 실시간 데이터 처리
4. **세션 관리**: Stateless JWT 보완을 위한 세션 정보 저장

### Redis 구성
- **버전**: Redis 7.0 (Alpine Linux)
- **메모리**: 512MB (프로덕션)
- **지속성**: AOF + RDB 백업
- **정책**: allkeys-lru (메모리 부족 시 LRU 방식으로 삭제)

---

## 🗃️ 캐시 데이터 구조

### 1. 사용자 세션 캐시

#### 키 구조: `session:{user_id}`
```json
{
  "userId": "uuid-123",
  "email": "user@example.com",
  "name": "홍길동",
  "profileImageUrl": "https://...",
  "lastActivity": "2025-08-27T10:30:00Z",
  "deviceInfo": {
    "userAgent": "...",
    "ip": "192.168.1.1"
  }
}
```

#### TTL: 24시간
```redis
SETEX session:uuid-123 86400 '{"userId":"uuid-123",...}'
```

### 2. 채팅 관련 캐시

#### 활성 채팅방 목록: `user:chatrooms:{user_id}`
```json
[
  {
    "roomId": "room-uuid-456",
    "roomName": "John과의 채팅",
    "lastMessageTime": "2025-08-27T10:25:00Z",
    "unreadCount": 3
  }
]
```

#### 최근 메시지 캐시: `chatroom:recent:{room_id}`
```json
[
  {
    "messageId": "msg-uuid-789",
    "senderId": "uuid-123",
    "senderName": "홍길동",
    "content": "안녕하세요!",
    "messageType": "TEXT",
    "timestamp": "2025-08-27T10:30:00Z"
  }
]
```

#### TTL: 1시간 (자주 변경되는 데이터)
```redis
SETEX chatroom:recent:room-uuid-456 3600 '[{"messageId":"msg-uuid-789",...}]'
```

### 3. WebRTC 시그널링 캐시

#### 시그널링 룸: `webrtc:room:{room_id}`
```json
{
  "roomId": "webrtc-room-123",
  "participants": [
    {
      "userId": "uuid-123",
      "socketId": "socket-456",
      "status": "connected"
    }
  ],
  "config": {
    "iceServers": [...]
  }
}
```

#### TTL: 2시간 (세션 진행 시간 고려)
```redis
SETEX webrtc:room:webrtc-room-123 7200 '{"roomId":"webrtc-room-123",...}'
```

### 4. 레벨 테스트 임시 데이터

#### 진행 중인 테스트: `leveltest:progress:{test_id}`
```json
{
  "testId": "test-uuid-123",
  "userId": "uuid-456",
  "currentQuestionIndex": 2,
  "responses": [
    {
      "questionId": 1,
      "response": "audio_file_url",
      "submittedAt": "2025-08-27T10:15:00Z"
    }
  ],
  "startedAt": "2025-08-27T10:00:00Z"
}
```

#### TTL: 1시간 (테스트 제한 시간)
```redis
SETEX leveltest:progress:test-uuid-123 3600 '{"testId":"test-uuid-123",...}'
```

---

## ⚡ 캐시 패턴

### 1. Cache-Aside Pattern (가장 많이 사용)

#### 읽기 패턴
```java
@Service
public class UserService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    public UserDto getUserProfile(String userId) {
        String cacheKey = "user:profile:" + userId;
        
        // 1. 캐시에서 먼저 조회
        UserDto cachedUser = (UserDto) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUser != null) {
            return cachedUser; // 캐시 히트
        }
        
        // 2. 캐시 미스: DB에서 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        UserDto userDto = UserMapper.toDto(user);
        
        // 3. 캐시에 저장 (TTL: 1시간)
        redisTemplate.opsForValue().set(cacheKey, userDto, Duration.ofHours(1));
        
        return userDto;
    }
}
```

#### 쓰기 패턴 (Write-Through)
```java
public void updateUserProfile(String userId, UpdateUserRequest request) {
    // 1. DB 업데이트
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    
    user.updateProfile(request);
    userRepository.save(user);
    
    // 2. 캐시 업데이트
    String cacheKey = "user:profile:" + userId;
    UserDto updatedUser = UserMapper.toDto(user);
    redisTemplate.opsForValue().set(cacheKey, updatedUser, Duration.ofHours(1));
    
    // 3. 관련 캐시 무효화 (채팅방 목록 등)
    invalidateRelatedCache(userId);
}
```

### 2. Write-Behind Pattern (대량 쓰기 최적화)

#### 채팅 메시지 처리
```java
@Component
public class ChatMessageProcessor {
    
    private final Queue<ChatMessage> messageQueue = new ConcurrentLinkedQueue<>();
    
    @EventListener
    public void handleChatMessage(ChatMessageEvent event) {
        ChatMessage message = event.getMessage();
        
        // 1. 즉시 캐시에 저장 (실시간성)
        String cacheKey = "chatroom:recent:" + message.getRoomId();
        redisTemplate.opsForList().leftPush(cacheKey, message);
        redisTemplate.opsForList().trim(cacheKey, 0, 99); // 최근 100개만 유지
        
        // 2. 큐에 추가 (배치 DB 저장)
        messageQueue.offer(message);
    }
    
    @Scheduled(fixedDelay = 5000) // 5초마다 배치 처리
    public void flushMessagesToDB() {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 큐에서 메시지 일괄 처리
        ChatMessage message;
        while ((message = messageQueue.poll()) != null && messages.size() < 100) {
            messages.add(message);
        }
        
        if (!messages.isEmpty()) {
            chatMessageRepository.saveAll(messages);
        }
    }
}
```

### 3. Refresh-Ahead Pattern (예측적 갱신)

#### 사용자 통계 데이터
```java
@Component
public class UserStatsCache {
    
    @Scheduled(fixedRate = 300000) // 5분마다 갱신
    public void refreshUserStats() {
        List<String> activeUserIds = getActiveUserIds();
        
        for (String userId : activeUserIds) {
            // 백그라운드에서 통계 데이터 미리 계산
            UserStats stats = calculateUserStats(userId);
            
            String cacheKey = "user:stats:" + userId;
            redisTemplate.opsForValue().set(cacheKey, stats, Duration.ofMinutes(10));
        }
    }
    
    private UserStats calculateUserStats(String userId) {
        // 복잡한 통계 계산 로직
        return userAnalyticsService.calculateStats(userId);
    }
}
```

---

## 🔧 Redis 설정 및 최적화

### Redis 설정 파일 (redis.conf)
```conf
# Memory Configuration
maxmemory 512mb
maxmemory-policy allkeys-lru

# Persistence Configuration
save 900 1        # 15분 동안 최소 1개 변경시 저장
save 300 10       # 5분 동안 최소 10개 변경시 저장
save 60 10000     # 1분 동안 최소 10000개 변경시 저장

# AOF Configuration
appendonly yes
appendfsync everysec
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# Network
tcp-keepalive 300
timeout 300

# Security
requirepass your-redis-password
```

### Spring Boot Redis 설정
```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: -1ms
```

### Redis Template 설정
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // 기본 TTL 30분
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

---

## 📊 캐시 모니터링

### 캐시 성능 지표
```java
@Component
public class CacheMetrics {
    
    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @EventListener
    public void onCacheHit(CacheHitEvent event) {
        Counter.builder("cache.hit")
            .tag("cache.name", event.getCacheName())
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void onCacheMiss(CacheMissEvent event) {
        Counter.builder("cache.miss")
            .tag("cache.name", event.getCacheName())
            .register(meterRegistry)
            .increment();
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다 측정
    public void recordRedisInfo() {
        Properties info = redisTemplate.getConnectionFactory()
            .getConnection()
            .info();
        
        // 메모리 사용량
        Gauge.builder("redis.memory.used")
            .register(meterRegistry, () -> parseMemoryUsed(info));
        
        // 히트율
        Gauge.builder("redis.hit.ratio")
            .register(meterRegistry, () -> calculateHitRatio(info));
    }
}
```

### Redis 명령어를 통한 모니터링
```bash
# 메모리 사용량 확인
redis-cli INFO memory

# 히트/미스 통계
redis-cli INFO stats

# 느린 명령어 확인
redis-cli SLOWLOG GET 10

# 실시간 명령어 모니터링
redis-cli MONITOR
```

---

## 🧹 캐시 무효화 전략

### 1. Time-based Invalidation (TTL)
```java
// 사용자 프로필: 자주 변경되지 않음, 긴 TTL
@Cacheable(value = "user:profile", key = "#userId")
@CacheEvict(value = "user:profile", key = "#userId", condition = "#result != null")
public UserDto getUserProfile(String userId) {
    // TTL: 1시간
    return userService.findById(userId);
}

// 채팅방 목록: 자주 변경됨, 짧은 TTL  
@Cacheable(value = "user:chatrooms", key = "#userId")
public List<ChatRoomDto> getUserChatRooms(String userId) {
    // TTL: 5분
    return chatService.findChatRoomsByUserId(userId);
}
```

### 2. Event-based Invalidation
```java
@Component
public class CacheInvalidationHandler {
    
    @EventListener
    public void handleUserProfileUpdate(UserProfileUpdatedEvent event) {
        String userId = event.getUserId();
        
        // 직접 관련 캐시 무효화
        cacheManager.evict("user:profile", userId);
        cacheManager.evict("user:stats", userId);
        
        // 간접 관련 캐시 무효화
        invalidateChatRoomCache(userId);
        invalidateMatchingCache(userId);
    }
    
    @EventListener
    public void handleChatMessage(ChatMessageEvent event) {
        String roomId = event.getRoomId();
        List<String> participantIds = event.getParticipantIds();
        
        // 채팅방 캐시 무효화
        cacheManager.evict("chatroom:recent", roomId);
        
        // 참여자별 채팅방 목록 캐시 무효화
        participantIds.forEach(userId -> 
            cacheManager.evict("user:chatrooms", userId));
    }
}
```

### 3. Versioning Strategy
```java
@Service
public class VersionedCacheService {
    
    private static final String VERSION_KEY = "cache:version:";
    
    public <T> T getCachedData(String key, Supplier<T> dataSupplier, Duration ttl) {
        String versionKey = VERSION_KEY + key;
        String currentVersion = getCurrentVersion(versionKey);
        String versionedKey = key + ":" + currentVersion;
        
        T cachedData = (T) redisTemplate.opsForValue().get(versionedKey);
        
        if (cachedData == null) {
            cachedData = dataSupplier.get();
            redisTemplate.opsForValue().set(versionedKey, cachedData, ttl);
        }
        
        return cachedData;
    }
    
    public void invalidateCache(String key) {
        String versionKey = VERSION_KEY + key;
        redisTemplate.opsForValue().increment(versionKey); // 버전 증가
    }
}
```

---

## 🚨 장애 대응

### 1. Redis 장애 시 대응
```java
@Component
public class ResilientCacheService {
    
    @Retryable(value = {RedisConnectionFailureException.class}, maxAttempts = 3)
    public <T> T getFromCache(String key, Class<T> type) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis connection failed, attempting retry: {}", e.getMessage());
            throw e; // Retry 어노테이션이 재시도 처리
        }
    }
    
    @Recover
    public <T> T recover(RedisConnectionFailureException e, String key, Class<T> type) {
        log.error("Redis is unavailable, falling back to database: {}", e.getMessage());
        return null; // 캐시 미스로 처리, DB에서 조회하도록 함
    }
}
```

### 2. Circuit Breaker 패턴
```java
@Component
public class CacheCircuitBreaker {
    
    private final CircuitBreaker circuitBreaker;
    
    public CacheCircuitBreaker() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("redisCache");
    }
    
    public <T> Optional<T> getCachedData(String key, Class<T> type) {
        Supplier<Optional<T>> cacheSupplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> {
                T value = (T) redisTemplate.opsForValue().get(key);
                return Optional.ofNullable(value);
            });
        
        try {
            return cacheSupplier.get();
        } catch (Exception e) {
            log.warn("Cache access failed, circuit breaker opened: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
```

---

## 📈 성능 최적화

### 1. Pipeline 사용
```java
public void batchUpdateCache(Map<String, Object> keyValuePairs) {
    redisTemplate.executePipelined(new SessionCallback<Object>() {
        @Override
        public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
            keyValuePairs.forEach((key, value) -> {
                operations.opsForValue().set((K) key, (V) value, Duration.ofHours(1));
            });
            return null;
        }
    });
}
```

### 2. Lua Script 사용
```java
@Component
public class LuaScriptCache {
    
    private final RedisScript<Long> incrementWithExpire;
    
    public LuaScriptCache() {
        this.incrementWithExpire = RedisScript.of(
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return current",
            Long.class
        );
    }
    
    public Long incrementCounter(String key, int expireSeconds) {
        return redisTemplate.execute(incrementWithExpire, 
            Collections.singletonList(key), 
            String.valueOf(expireSeconds));
    }
}
```

### 3. 압축 사용
```java
@Bean
public RedisTemplate<String, Object> compressedRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    
    // 압축을 지원하는 직렬화 설정
    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
    
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new CompressingRedisSerializer(serializer));
    
    return template;
}
```

---

## 📚 관련 문서

- [데이터베이스 스키마](./database-schema.md)
- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [백엔드 서비스](../07-backend/services-overview.md)
- [성능 모니터링](../08-infrastructure/monitoring.md)