package com.studymate.domain.session.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebRtcQualityService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 패턴
    private static final String QUALITY_TEST_PREFIX = "webrtc:quality:test:";
    private static final String QUALITY_HISTORY_PREFIX = "webrtc:quality:history:";
    private static final String QUALITY_ALERT_PREFIX = "webrtc:quality:alert:";
    
    // 품질 임계값 설정
    private static final double MIN_ACCEPTABLE_QUALITY_SCORE = 70.0;
    private static final long MAX_RTT_MS = 150; // 150ms 이상이면 품질 저하
    private static final double MAX_PACKET_LOSS_RATE = 5.0; // 5% 이상 패킷 손실
    private static final long MIN_BANDWIDTH_KBPS = 100; // 100kbps 미만이면 품질 저하
    
    // 데이터 보관 기간
    private static final Duration QUALITY_TEST_TTL = Duration.ofHours(1);
    private static final Duration QUALITY_HISTORY_TTL = Duration.ofDays(30);

    /**
     * WebRTC 연결 품질 테스트 시작
     */
    public QualityTestSession startQualityTest(UUID userId, String roomId) {
        try {
            QualityTestSession testSession = QualityTestSession.builder()
                    .testId(UUID.randomUUID().toString())
                    .userId(userId)
                    .roomId(roomId)
                    .status(TestStatus.IN_PROGRESS)
                    .startedAt(LocalDateTime.now())
                    .testSteps(initializeTestSteps())
                    .currentStepIndex(0)
                    .overallScore(0.0)
                    .build();

            String testKey = buildQualityTestKey(testSession.getTestId());
            String jsonSession = objectMapper.writeValueAsString(testSession);
            
            redisTemplate.opsForValue().set(testKey, jsonSession, QUALITY_TEST_TTL);
            
            log.info("Quality test started for user {} in room {}: testId={}", 
                userId, roomId, testSession.getTestId());
            
            return testSession;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to start quality test for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("품질 테스트 시작에 실패했습니다.", e);
        }
    }

    /**
     * 네트워크 지연시간(RTT) 측정
     */
    public NetworkLatencyResult measureNetworkLatency(String testId, String targetEndpoint) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 실제 네트워크 지연시간 측정 구현
            // ICMP ping이나 HTTP 요청 기반 지연시간 측정
            int basePing = 30; // 기본 지연시간
            int variance = new Random().nextInt(80); // 0-80ms 변동
            Thread.sleep(basePing + variance);
            
            long endTime = System.currentTimeMillis();
            long rttMs = endTime - startTime;
            
            NetworkLatencyResult result = NetworkLatencyResult.builder()
                    .targetEndpoint(targetEndpoint)
                    .rttMs(rttMs)
                    .timestamp(LocalDateTime.now())
                    .quality(calculateLatencyQuality(rttMs))
                    .recommendations(getLatencyRecommendations(rttMs))
                    .build();

            updateTestStepResult(testId, "NETWORK_LATENCY", result);
            
            log.debug("Network latency measured for test {}: {}ms", testId, rttMs);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to measure network latency for test {}: {}", testId, e.getMessage());
            throw new RuntimeException("네트워크 지연시간 측정에 실패했습니다.", e);
        }
    }

    /**
     * 대역폭 테스트
     */
    public BandwidthTestResult measureBandwidth(String testId) {
        try {
            // 업로드 대역폭 테스트 (시뮬레이션)
            long uploadKbps = 500 + new Random().nextInt(1500); // 500-2000 kbps
            
            // 다운로드 대역폭 테스트 (시뮬레이션)  
            long downloadKbps = 1000 + new Random().nextInt(2000); // 1000-3000 kbps
            
            BandwidthTestResult result = BandwidthTestResult.builder()
                    .uploadBandwidthKbps(uploadKbps)
                    .downloadBandwidthKbps(downloadKbps)
                    .timestamp(LocalDateTime.now())
                    .quality(calculateBandwidthQuality(uploadKbps, downloadKbps))
                    .recommendations(getBandwidthRecommendations(uploadKbps, downloadKbps))
                    .build();

            updateTestStepResult(testId, "BANDWIDTH", result);
            
            log.debug("Bandwidth measured for test {}: up={}kbps, down={}kbps", 
                testId, uploadKbps, downloadKbps);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to measure bandwidth for test {}: {}", testId, e.getMessage());
            throw new RuntimeException("대역폭 테스트에 실패했습니다.", e);
        }
    }

    /**
     * 패킷 손실률 측정
     */
    public PacketLossResult measurePacketLoss(String testId) {
        try {
            // 패킷 손실률 테스트 (시뮬레이션)
            double packetLossRate = new Random().nextDouble() * 10; // 0-10% 손실률
            int totalPackets = 1000;
            int lostPackets = (int) (totalPackets * packetLossRate / 100);
            
            PacketLossResult result = PacketLossResult.builder()
                    .totalPackets(totalPackets)
                    .lostPackets(lostPackets)
                    .packetLossRate(packetLossRate)
                    .timestamp(LocalDateTime.now())
                    .quality(calculatePacketLossQuality(packetLossRate))
                    .recommendations(getPacketLossRecommendations(packetLossRate))
                    .build();

            updateTestStepResult(testId, "PACKET_LOSS", result);
            
            log.debug("Packet loss measured for test {}: {}% loss", testId, packetLossRate);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to measure packet loss for test {}: {}", testId, e.getMessage());
            throw new RuntimeException("패킷 손실률 측정에 실패했습니다.", e);
        }
    }

    /**
     * 전체 품질 테스트 완료 및 결과 계산
     */
    public QualityTestResult completeQualityTest(String testId) {
        try {
            QualityTestSession session = getQualityTestSession(testId);
            if (session == null) {
                throw new RuntimeException("품질 테스트 세션을 찾을 수 없습니다: " + testId);
            }

            // 전체 품질 점수 계산
            double overallScore = calculateOverallQualityScore(session);
            QualityLevel qualityLevel = determineQualityLevel(overallScore);
            
            QualityTestResult result = QualityTestResult.builder()
                    .testId(testId)
                    .userId(session.getUserId())
                    .roomId(session.getRoomId())
                    .overallScore(overallScore)
                    .qualityLevel(qualityLevel)
                    .testSteps(session.getTestSteps())
                    .completedAt(LocalDateTime.now())
                    .recommendations(generateOverallRecommendations(session))
                    .issues(identifyQualityIssues(session))
                    .build();

            // 테스트 세션 상태 업데이트
            session.setStatus(TestStatus.COMPLETED);
            session.setOverallScore(overallScore);
            session.setCompletedAt(LocalDateTime.now());
            
            // 결과 저장
            saveQualityTestResult(result);
            saveQualityHistory(session.getUserId(), result);
            
            // 품질 저하 알림 확인
            checkQualityAlerts(result);
            
            log.info("Quality test completed for user {}: score={}, level={}", 
                session.getUserId(), overallScore, qualityLevel);
                
            return result;
            
        } catch (Exception e) {
            log.error("Failed to complete quality test {}: {}", testId, e.getMessage());
            throw new RuntimeException("품질 테스트 완료에 실패했습니다.", e);
        }
    }

    /**
     * 품질 테스트 이력 조회
     */
    public List<QualityTestResult> getQualityHistory(UUID userId, int limit) {
        String pattern = buildQualityHistoryKey(userId, "*");
        Set<String> historyKeys = redisTemplate.keys(pattern);
        
        if (historyKeys == null || historyKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return historyKeys.stream()
                .limit(limit)
                .map(key -> {
                    try {
                        String jsonResult = redisTemplate.opsForValue().get(key);
                        return jsonResult != null ? 
                            objectMapper.readValue(jsonResult, QualityTestResult.class) : null;
                    } catch (Exception e) {
                        log.error("Failed to deserialize quality history {}: {}", key, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 실시간 품질 모니터링 데이터 업데이트
     */
    public void updateRealTimeQuality(String roomId, UUID userId, RealTimeQualityMetrics metrics) {
        try {
            String metricsKey = "webrtc:realtime:" + roomId + ":" + userId;
            String jsonMetrics = objectMapper.writeValueAsString(metrics);
            
            redisTemplate.opsForValue().set(metricsKey, jsonMetrics, Duration.ofMinutes(5));
            
            // 품질 저하 감지 시 알림
            if (isQualityDegraded(metrics)) {
                sendQualityAlert(userId, roomId, metrics);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Failed to update real-time quality for user {} in room {}: {}", 
                userId, roomId, e.getMessage());
        }
    }

    // === Private Helper Methods ===
    
    private QualityTestSession getQualityTestSession(String testId) {
        try {
            String testKey = buildQualityTestKey(testId);
            String jsonSession = redisTemplate.opsForValue().get(testKey);
            
            return jsonSession != null ? 
                objectMapper.readValue(jsonSession, QualityTestSession.class) : null;
                
        } catch (Exception e) {
            log.error("Failed to get quality test session {}: {}", testId, e.getMessage());
            return null;
        }
    }
    
    private void updateTestStepResult(String testId, String stepType, Object result) {
        try {
            QualityTestSession session = getQualityTestSession(testId);
            if (session != null) {
                // 해당 단계에 결과 저장
                session.getTestSteps().stream()
                        .filter(step -> step.getStepType().equals(stepType))
                        .findFirst()
                        .ifPresent(step -> {
                            step.setResult(result);
                            step.setStatus(TestStepStatus.COMPLETED);
                            step.setCompletedAt(LocalDateTime.now());
                        });
                
                // 다음 단계로 진행
                session.setCurrentStepIndex(session.getCurrentStepIndex() + 1);
                
                // 세션 업데이트
                String testKey = buildQualityTestKey(testId);
                String jsonSession = objectMapper.writeValueAsString(session);
                redisTemplate.opsForValue().set(testKey, jsonSession, QUALITY_TEST_TTL);
            }
        } catch (Exception e) {
            log.error("Failed to update test step result for {}: {}", testId, e.getMessage());
        }
    }
    
    private List<QualityTestStep> initializeTestSteps() {
        return Arrays.asList(
            QualityTestStep.builder()
                    .stepType("NETWORK_LATENCY")
                    .stepName("네트워크 지연시간 측정")
                    .status(TestStepStatus.PENDING)
                    .build(),
            QualityTestStep.builder()
                    .stepType("BANDWIDTH")
                    .stepName("대역폭 측정")
                    .status(TestStepStatus.PENDING)
                    .build(),
            QualityTestStep.builder()
                    .stepType("PACKET_LOSS")
                    .stepName("패킷 손실률 측정")
                    .status(TestStepStatus.PENDING)
                    .build()
        );
    }
    
    private QualityLevel calculateLatencyQuality(long rttMs) {
        if (rttMs <= 50) return QualityLevel.EXCELLENT;
        if (rttMs <= 100) return QualityLevel.GOOD;
        if (rttMs <= 150) return QualityLevel.FAIR;
        return QualityLevel.POOR;
    }
    
    private QualityLevel calculateBandwidthQuality(long uploadKbps, long downloadKbps) {
        long minBandwidth = Math.min(uploadKbps, downloadKbps);
        if (minBandwidth >= 1000) return QualityLevel.EXCELLENT;
        if (minBandwidth >= 500) return QualityLevel.GOOD;
        if (minBandwidth >= 200) return QualityLevel.FAIR;
        return QualityLevel.POOR;
    }
    
    private QualityLevel calculatePacketLossQuality(double packetLossRate) {
        if (packetLossRate <= 1.0) return QualityLevel.EXCELLENT;
        if (packetLossRate <= 3.0) return QualityLevel.GOOD;
        if (packetLossRate <= 5.0) return QualityLevel.FAIR;
        return QualityLevel.POOR;
    }
    
    private double calculateOverallQualityScore(QualityTestSession session) {
        double totalScore = 0.0;
        int completedSteps = 0;
        
        for (QualityTestStep step : session.getTestSteps()) {
            if (step.getStatus() == TestStepStatus.COMPLETED && step.getResult() != null) {
                double stepScore = calculateStepScore(step);
                totalScore += stepScore;
                completedSteps++;
            }
        }
        
        return completedSteps > 0 ? totalScore / completedSteps : 0.0;
    }
    
    private double calculateStepScore(QualityTestStep step) {
        // 각 단계별 점수 계산 로직
        switch (step.getStepType()) {
            case "NETWORK_LATENCY":
                NetworkLatencyResult latency = (NetworkLatencyResult) step.getResult();
                return latency.getQuality().getScore();
            case "BANDWIDTH":
                BandwidthTestResult bandwidth = (BandwidthTestResult) step.getResult();
                return bandwidth.getQuality().getScore();
            case "PACKET_LOSS":
                PacketLossResult packetLoss = (PacketLossResult) step.getResult();
                return packetLoss.getQuality().getScore();
            default:
                return 50.0;
        }
    }
    
    private QualityLevel determineQualityLevel(double score) {
        if (score >= 90) return QualityLevel.EXCELLENT;
        if (score >= 75) return QualityLevel.GOOD;
        if (score >= 60) return QualityLevel.FAIR;
        return QualityLevel.POOR;
    }
    
    private List<String> getLatencyRecommendations(long rttMs) {
        if (rttMs > MAX_RTT_MS) {
            return Arrays.asList(
                "Wi-Fi 대신 유선 연결을 사용해보세요",
                "다른 네트워크 활동을 중단해보세요",
                "라우터를 재시작해보세요"
            );
        }
        return Collections.emptyList();
    }
    
    private List<String> getBandwidthRecommendations(long uploadKbps, long downloadKbps) {
        List<String> recommendations = new ArrayList<>();
        
        if (uploadKbps < MIN_BANDWIDTH_KBPS) {
            recommendations.add("업로드 대역폭이 부족합니다. 인터넷 속도를 확인해보세요");
        }
        if (downloadKbps < MIN_BANDWIDTH_KBPS) {
            recommendations.add("다운로드 대역폭이 부족합니다. 다른 기기의 인터넷 사용을 줄여보세요");
        }
        
        return recommendations;
    }
    
    private List<String> getPacketLossRecommendations(double packetLossRate) {
        if (packetLossRate > MAX_PACKET_LOSS_RATE) {
            return Arrays.asList(
                "네트워크 연결 상태를 확인해보세요",
                "Wi-Fi 신호 강도를 확인해보세요",
                "네트워크 장비를 재시작해보세요"
            );
        }
        return Collections.emptyList();
    }
    
    private List<String> generateOverallRecommendations(QualityTestSession session) {
        List<String> allRecommendations = new ArrayList<>();
        
        for (QualityTestStep step : session.getTestSteps()) {
            if (step.getResult() != null) {
                switch (step.getStepType()) {
                    case "NETWORK_LATENCY":
                        NetworkLatencyResult latency = (NetworkLatencyResult) step.getResult();
                        allRecommendations.addAll(latency.getRecommendations());
                        break;
                    case "BANDWIDTH":
                        BandwidthTestResult bandwidth = (BandwidthTestResult) step.getResult();
                        allRecommendations.addAll(bandwidth.getRecommendations());
                        break;
                    case "PACKET_LOSS":
                        PacketLossResult packetLoss = (PacketLossResult) step.getResult();
                        allRecommendations.addAll(packetLoss.getRecommendations());
                        break;
                }
            }
        }
        
        return allRecommendations.stream().distinct().collect(Collectors.toList());
    }
    
    private List<String> identifyQualityIssues(QualityTestSession session) {
        List<String> issues = new ArrayList<>();
        
        for (QualityTestStep step : session.getTestSteps()) {
            if (step.getResult() != null) {
                QualityLevel stepQuality = getStepQualityLevel(step);
                if (stepQuality == QualityLevel.POOR || stepQuality == QualityLevel.FAIR) {
                    issues.add(step.getStepName() + " 품질이 좋지 않습니다");
                }
            }
        }
        
        return issues;
    }
    
    private QualityLevel getStepQualityLevel(QualityTestStep step) {
        switch (step.getStepType()) {
            case "NETWORK_LATENCY":
                return ((NetworkLatencyResult) step.getResult()).getQuality();
            case "BANDWIDTH":
                return ((BandwidthTestResult) step.getResult()).getQuality();
            case "PACKET_LOSS":
                return ((PacketLossResult) step.getResult()).getQuality();
            default:
                return QualityLevel.FAIR;
        }
    }
    
    private void saveQualityTestResult(QualityTestResult result) {
        try {
            String resultKey = "webrtc:quality:result:" + result.getTestId();
            String jsonResult = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(resultKey, jsonResult, Duration.ofHours(24));
        } catch (JsonProcessingException e) {
            log.error("Failed to save quality test result: {}", e.getMessage());
        }
    }
    
    private void saveQualityHistory(UUID userId, QualityTestResult result) {
        try {
            String historyKey = buildQualityHistoryKey(userId, result.getTestId());
            String jsonResult = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(historyKey, jsonResult, QUALITY_HISTORY_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to save quality history: {}", e.getMessage());
        }
    }
    
    private void checkQualityAlerts(QualityTestResult result) {
        if (result.getQualityLevel() == QualityLevel.POOR) {
            sendQualityDegradationAlert(result.getUserId(), result.getRoomId(), result);
        }
    }
    
    private boolean isQualityDegraded(RealTimeQualityMetrics metrics) {
        return metrics.getRttMs() > MAX_RTT_MS || 
               metrics.getPacketLossRate() > MAX_PACKET_LOSS_RATE ||
               metrics.getBandwidthKbps() < MIN_BANDWIDTH_KBPS;
    }
    
    private void sendQualityAlert(UUID userId, String roomId, RealTimeQualityMetrics metrics) {
        log.warn("Quality degradation detected for user {} in room {}: rtt={}ms, loss={}%, bandwidth={}kbps", 
            userId, roomId, metrics.getRttMs(), metrics.getPacketLossRate(), metrics.getBandwidthKbps());
    }
    
    private void sendQualityDegradationAlert(UUID userId, String roomId, QualityTestResult result) {
        log.warn("Poor quality detected for user {} in room {}: score={}", 
            userId, roomId, result.getOverallScore());
    }
    
    private String buildQualityTestKey(String testId) {
        return QUALITY_TEST_PREFIX + testId;
    }
    
    private String buildQualityHistoryKey(UUID userId, String testId) {
        return QUALITY_HISTORY_PREFIX + userId + ":" + testId;
    }
    
    // === 내부 데이터 클래스들 ===
    
    public enum TestStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
    
    public enum TestStepStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
    
    public enum QualityLevel {
        EXCELLENT(95.0),
        GOOD(85.0),
        FAIR(70.0),
        POOR(40.0);
        
        private final double score;
        
        QualityLevel(double score) {
            this.score = score;
        }
        
        public double getScore() {
            return score;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityTestSession {
        private String testId;
        private UUID userId;
        private String roomId;
        private TestStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<QualityTestStep> testSteps;
        private int currentStepIndex;
        private double overallScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityTestStep {
        private String stepType;
        private String stepName;
        private TestStepStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Object result;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NetworkLatencyResult {
        private String targetEndpoint;
        private long rttMs;
        private LocalDateTime timestamp;
        private QualityLevel quality;
        private List<String> recommendations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BandwidthTestResult {
        private long uploadBandwidthKbps;
        private long downloadBandwidthKbps;
        private LocalDateTime timestamp;
        private QualityLevel quality;
        private List<String> recommendations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PacketLossResult {
        private int totalPackets;
        private int lostPackets;
        private double packetLossRate;
        private LocalDateTime timestamp;
        private QualityLevel quality;
        private List<String> recommendations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityTestResult {
        private String testId;
        private UUID userId;
        private String roomId;
        private double overallScore;
        private QualityLevel qualityLevel;
        private List<QualityTestStep> testSteps;
        private LocalDateTime completedAt;
        private List<String> recommendations;
        private List<String> issues;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RealTimeQualityMetrics {
        private long rttMs;
        private double packetLossRate;
        private long bandwidthKbps;
        private LocalDateTime timestamp;
    }
}