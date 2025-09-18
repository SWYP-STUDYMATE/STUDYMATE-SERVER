package com.studymate.domain.webrtc.dto.request;

import jakarta.validation.constraints.NotNull;

public class WebRTCRoomSyncRequest {

    @NotNull(message = "sessionId는 필수입니다.")
    private Long sessionId;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
