package com.studymate.domain.session.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcJoinRequest {
    
    private UUID userId;
    private String peerId;
    private String deviceInfo;
    private Boolean cameraEnabled = true;
    private Boolean microphoneEnabled = true;
    
    // 선택적 연결 설정
    private String preferredVideoQuality = "HD"; // HD, SD, LOW
    private String preferredAudioQuality = "HIGH"; // HIGH, MEDIUM, LOW
    private Boolean enableScreenShare = false;
    
    // 클라이언트 정보
    private String browserInfo;
    private String osInfo;
    private String networkType; // WIFI, CELLULAR, ETHERNET
    
    public WebRtcJoinRequest(UUID userId, String peerId) {
        this.userId = userId;
        this.peerId = peerId;
    }
}