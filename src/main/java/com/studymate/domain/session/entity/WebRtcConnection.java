package com.studymate.domain.session.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "webrtc_connections", indexes = {
    @Index(name = "idx_webrtc_connection_room", columnList = "webrtc_room_id"),
    @Index(name = "idx_webrtc_connection_from_to", columnList = "from_peer_id, to_peer_id"),
    @Index(name = "idx_webrtc_connection_status", columnList = "connection_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcConnection extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webrtc_room_id", nullable = false)
    private WebRtcRoom webRtcRoom;
    
    @Column(name = "from_peer_id", nullable = false)
    private String fromPeerId;
    
    @Column(name = "to_peer_id", nullable = false)
    private String toPeerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private ConnectionStatus connectionStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private ConnectionType connectionType;
    
    @Column(name = "offer_sdp", columnDefinition = "TEXT")
    private String offerSdp;
    
    @Column(name = "answer_sdp", columnDefinition = "TEXT")
    private String answerSdp;
    
    @Column(name = "ice_candidates", columnDefinition = "JSON")
    private String iceCandidates; // JSON array of ICE candidates
    
    @Column(name = "established_at")
    private LocalDateTime establishedAt;
    
    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;
    
    @Column(name = "termination_reason")
    private String terminationReason;
    
    // Connection quality metrics
    @Builder.Default
    @Column(name = "bytes_sent")
    private Long bytesSent = 0L;
    
    @Builder.Default
    @Column(name = "bytes_received")
    private Long bytesReceived = 0L;
    
    @Builder.Default
    @Column(name = "packets_sent")
    private Long packetsSent = 0L;
    
    @Builder.Default
    @Column(name = "packets_received")
    private Long packetsReceived = 0L;
    
    @Builder.Default
    @Column(name = "packets_lost")
    private Long packetsLost = 0L;
    
    @Column(name = "current_round_trip_time")
    private Double currentRoundTripTime; // in seconds
    
    @Column(name = "available_outgoing_bitrate")
    private Long availableOutgoingBitrate; // in bps
    
    @Column(name = "available_incoming_bitrate")
    private Long availableIncomingBitrate; // in bps
    
    @Column(name = "last_stats_update")
    private LocalDateTime lastStatsUpdate;
    
    public enum ConnectionStatus {
        INITIATING("시작 중"),
        OFFER_SENT("제안 전송됨"),
        ANSWER_SENT("응답 전송됨"),
        ESTABLISHED("연결됨"),
        FAILED("실패"),
        CLOSED("종료됨");
        
        private final String description;
        
        ConnectionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ConnectionType {
        PEER_TO_PEER("P2P 연결"),
        RELAY("릴레이 연결"),
        HOST("호스트 연결"),
        SRFLX("서버 반사 연결"),
        PRFLX("피어 반사 연결");
        
        private final String description;
        
        ConnectionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static WebRtcConnection createConnection(WebRtcRoom room, String fromPeerId, 
                                                  String toPeerId, ConnectionType type) {
        return WebRtcConnection.builder()
                .webRtcRoom(room)
                .fromPeerId(fromPeerId)
                .toPeerId(toPeerId)
                .connectionStatus(ConnectionStatus.INITIATING)
                .connectionType(type)
                .bytesSent(0L)
                .bytesReceived(0L)
                .packetsSent(0L)
                .packetsReceived(0L)
                .packetsLost(0L)
                .build();
    }
    
    public void setOffer(String sdp) {
        this.offerSdp = sdp;
        this.connectionStatus = ConnectionStatus.OFFER_SENT;
    }
    
    public void setAnswer(String sdp) {
        this.answerSdp = sdp;
        this.connectionStatus = ConnectionStatus.ANSWER_SENT;
    }
    
    public void establish() {
        this.connectionStatus = ConnectionStatus.ESTABLISHED;
        this.establishedAt = LocalDateTime.now();
    }
    
    public void fail(String reason) {
        this.connectionStatus = ConnectionStatus.FAILED;
        this.terminatedAt = LocalDateTime.now();
        this.terminationReason = reason;
    }
    
    public void close(String reason) {
        this.connectionStatus = ConnectionStatus.CLOSED;
        this.terminatedAt = LocalDateTime.now();
        this.terminationReason = reason;
    }
    
    public void updateStats(Long bytesSent, Long bytesReceived, Long packetsSent, 
                           Long packetsReceived, Long packetsLost, Double rtt,
                           Long outgoingBitrate, Long incomingBitrate) {
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.packetsSent = packetsSent;
        this.packetsReceived = packetsReceived;
        this.packetsLost = packetsLost;
        this.currentRoundTripTime = rtt;
        this.availableOutgoingBitrate = outgoingBitrate;
        this.availableIncomingBitrate = incomingBitrate;
        this.lastStatsUpdate = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return this.connectionStatus == ConnectionStatus.ESTABLISHED;
    }
    
    public double getPacketLossRate() {
        if (packetsReceived == 0) return 0.0;
        return (double) packetsLost / (packetsReceived + packetsLost) * 100;
    }
}