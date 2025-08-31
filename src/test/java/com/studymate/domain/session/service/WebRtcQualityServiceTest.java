package com.studymate.domain.session.service;

import com.studymate.domain.session.domain.dto.request.WebRtcQualityTestRequest;
import com.studymate.domain.session.domain.dto.response.WebRtcQualityTestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebRtcQualityService 단위 테스트")
class WebRtcQualityServiceTest {

    @InjectMocks
    private WebRtcQualityService webRtcQualityService;

    private UUID userId;
    private WebRtcQualityTestRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        request = WebRtcQualityTestRequest.builder()
                .userId(userId)
                .deviceType("desktop")
                .browserType("chrome")
                .hasCamera(true)
                .hasMicrophone(true)
                .build();
    }

    @Test
    @DisplayName("품질 테스트 실행 - 우수한 품질")
    void performQualityTest_ExcellentQuality() {
        // Given
        request.setLatency(20);
        request.setBandwidth(50.0);
        request.setPacketLoss(0.1);
        request.setJitter(5);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getOverallScore()).isGreaterThan(80);
        assertThat(response.getQualityLevel()).isEqualTo("EXCELLENT");
        assertThat(response.isRecommendedForVideo()).isTrue();
        assertThat(response.getLatencyScore()).isGreaterThan(90);
        assertThat(response.getBandwidthScore()).isGreaterThan(90);
        assertThat(response.getPacketLossScore()).isGreaterThan(90);
        assertThat(response.getJitterScore()).isGreaterThan(90);
    }

    @Test
    @DisplayName("품질 테스트 실행 - 양호한 품질")
    void performQualityTest_GoodQuality() {
        // Given
        request.setLatency(80);
        request.setBandwidth(30.0);
        request.setPacketLoss(1.5);
        request.setJitter(20);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getOverallScore()).isBetween(60, 80);
        assertThat(response.getQualityLevel()).isEqualTo("GOOD");
        assertThat(response.isRecommendedForVideo()).isTrue();
    }

    @Test
    @DisplayName("품질 테스트 실행 - 보통 품질")
    void performQualityTest_FairQuality() {
        // Given
        request.setLatency(150);
        request.setBandwidth(15.0);
        request.setPacketLoss(3.0);
        request.setJitter(40);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getOverallScore()).isBetween(40, 60);
        assertThat(response.getQualityLevel()).isEqualTo("FAIR");
        assertThat(response.isRecommendedForVideo()).isFalse();
    }

    @Test
    @DisplayName("품질 테스트 실행 - 낮은 품질")
    void performQualityTest_PoorQuality() {
        // Given
        request.setLatency(300);
        request.setBandwidth(5.0);
        request.setPacketLoss(8.0);
        request.setJitter(100);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getOverallScore()).isLessThan(40);
        assertThat(response.getQualityLevel()).isEqualTo("POOR");
        assertThat(response.isRecommendedForVideo()).isFalse();
        assertThat(response.getRecommendations()).contains("인터넷 연결 상태를 확인해주세요");
    }

    @Test
    @DisplayName("지연시간 점수 계산")
    void calculateLatencyScore_VariousLatencies() {
        // Excellent latency (< 50ms)
        assertThat(webRtcQualityService.calculateLatencyScore(30)).isEqualTo(100);
        
        // Good latency (50-100ms)
        assertThat(webRtcQualityService.calculateLatencyScore(75)).isEqualTo(80);
        
        // Fair latency (100-200ms)
        assertThat(webRtcQualityService.calculateLatencyScore(150)).isEqualTo(60);
        
        // Poor latency (> 200ms)
        assertThat(webRtcQualityService.calculateLatencyScore(250)).isEqualTo(30);
    }

    @Test
    @DisplayName("대역폭 점수 계산")
    void calculateBandwidthScore_VariousBandwidths() {
        // Excellent bandwidth (> 25 Mbps)
        assertThat(webRtcQualityService.calculateBandwidthScore(50.0)).isEqualTo(100);
        
        // Good bandwidth (10-25 Mbps)
        assertThat(webRtcQualityService.calculateBandwidthScore(20.0)).isEqualTo(80);
        
        // Fair bandwidth (5-10 Mbps)
        assertThat(webRtcQualityService.calculateBandwidthScore(7.5)).isEqualTo(60);
        
        // Poor bandwidth (< 5 Mbps)
        assertThat(webRtcQualityService.calculateBandwidthScore(3.0)).isEqualTo(30);
    }

    @Test
    @DisplayName("패킷 손실 점수 계산")
    void calculatePacketLossScore_VariousLossRates() {
        // Excellent packet loss (< 1%)
        assertThat(webRtcQualityService.calculatePacketLossScore(0.5)).isEqualTo(100);
        
        // Good packet loss (1-2%)
        assertThat(webRtcQualityService.calculatePacketLossScore(1.5)).isEqualTo(80);
        
        // Fair packet loss (2-5%)
        assertThat(webRtcQualityService.calculatePacketLossScore(3.0)).isEqualTo(60);
        
        // Poor packet loss (> 5%)
        assertThat(webRtcQualityService.calculatePacketLossScore(7.0)).isEqualTo(30);
    }

    @Test
    @DisplayName("지터 점수 계산")
    void calculateJitterScore_VariousJitterValues() {
        // Excellent jitter (< 10ms)
        assertThat(webRtcQualityService.calculateJitterScore(5)).isEqualTo(100);
        
        // Good jitter (10-30ms)
        assertThat(webRtcQualityService.calculateJitterScore(20)).isEqualTo(80);
        
        // Fair jitter (30-50ms)
        assertThat(webRtcQualityService.calculateJitterScore(40)).isEqualTo(60);
        
        // Poor jitter (> 50ms)
        assertThat(webRtcQualityService.calculateJitterScore(80)).isEqualTo(30);
    }

    @Test
    @DisplayName("품질 레벨 결정")
    void determineQualityLevel_VariousScores() {
        assertThat(webRtcQualityService.determineQualityLevel(90)).isEqualTo("EXCELLENT");
        assertThat(webRtcQualityService.determineQualityLevel(70)).isEqualTo("GOOD");
        assertThat(webRtcQualityService.determineQualityLevel(50)).isEqualTo("FAIR");
        assertThat(webRtcQualityService.determineQualityLevel(30)).isEqualTo("POOR");
    }

    @Test
    @DisplayName("개선 권장사항 생성 - 낮은 품질")
    void generateRecommendations_PoorQuality() {
        // Given
        request.setLatency(300);
        request.setBandwidth(3.0);
        request.setPacketLoss(8.0);
        request.setJitter(100);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations()).contains("인터넷 연결 상태를 확인해주세요");
        assertThat(response.getRecommendations()).contains("다른 애플리케이션을 종료하여 대역폭을 확보해주세요");
        assertThat(response.getRecommendations()).contains("유선 연결을 사용해보세요");
    }

    @Test
    @DisplayName("개선 권장사항 생성 - 우수한 품질")
    void generateRecommendations_ExcellentQuality() {
        // Given
        request.setLatency(20);
        request.setBandwidth(50.0);
        request.setPacketLoss(0.1);
        request.setJitter(5);

        // When
        WebRtcQualityTestResponse response = webRtcQualityService.performQualityTest(request);

        // Then
        assertThat(response.getRecommendations()).contains("현재 연결 품질이 우수합니다");
    }
}