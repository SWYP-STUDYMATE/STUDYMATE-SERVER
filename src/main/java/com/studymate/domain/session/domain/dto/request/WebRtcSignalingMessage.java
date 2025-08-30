package com.studymate.domain.session.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcSignalingMessage {

    @NotNull(message = "세션 ID는 필수입니다.")
    private UUID sessionId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    @NotBlank(message = "메시지 타입은 필수입니다.")
    private String type; // offer, answer, ice-candidate, join, leave

    @NotBlank(message = "메시지 데이터는 필수입니다.")
    private String data; // JSON string containing SDP, ICE candidate, etc.

    private String targetUserId; // For direct peer-to-peer messages

    private Long timestamp;

    private String connectionId; // Unique connection identifier

    // WebRTC 신호 타입 enum
    public enum SignalType {
        OFFER("offer"),
        ANSWER("answer"),
        ICE_CANDIDATE("ice-candidate"),
        JOIN("join"),
        LEAVE("leave"),
        MUTE("mute"),
        UNMUTE("unmute"),
        VIDEO_ON("video-on"),
        VIDEO_OFF("video-off"),
        SCREEN_SHARE_START("screen-share-start"),
        SCREEN_SHARE_STOP("screen-share-stop");

        private final String value;

        SignalType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public boolean isOffer() {
        return SignalType.OFFER.getValue().equals(this.type);
    }

    public boolean isAnswer() {
        return SignalType.ANSWER.getValue().equals(this.type);
    }

    public boolean isIceCandidate() {
        return SignalType.ICE_CANDIDATE.getValue().equals(this.type);
    }

    public boolean isMediaControl() {
        return SignalType.MUTE.getValue().equals(this.type) ||
               SignalType.UNMUTE.getValue().equals(this.type) ||
               SignalType.VIDEO_ON.getValue().equals(this.type) ||
               SignalType.VIDEO_OFF.getValue().equals(this.type);
    }

    public boolean isScreenShare() {
        return SignalType.SCREEN_SHARE_START.getValue().equals(this.type) ||
               SignalType.SCREEN_SHARE_STOP.getValue().equals(this.type);
    }
}