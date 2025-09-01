package com.studymate.domain.session.controller;

import com.studymate.domain.session.domain.dto.request.WebRtcJoinRequest;
import com.studymate.domain.session.domain.dto.request.WebRtcSignalingMessage;
import com.studymate.domain.session.domain.dto.response.WebRtcRoomResponse;
import com.studymate.domain.session.domain.dto.response.WebRtcParticipantResponse;
import com.studymate.domain.session.domain.dto.response.WebRtcConnectionStatsResponse;
import com.studymate.domain.session.service.WebRtcService;
import com.studymate.common.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
// @RestController - WebRTC는 Cloudflare Workers에서 P2P로 처리됨
@RequestMapping("/api/v1/webrtc")
@RequiredArgsConstructor
@Tag(name = "WebRTC", description = "WebRTC 세션 관리 API")
public class WebRtcController {

    private final WebRtcService webRtcService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(
            summary = "WebRTC 룸 생성",
            description = "세션을 위한 WebRTC 룸을 생성합니다."
    )
    @PostMapping("/rooms/{sessionId}")
    public ApiResponse<WebRtcRoomResponse> createRoom(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable Long sessionId,
            @Parameter(description = "호스트 사용자 ID", required = true)
            @RequestParam UUID hostUserId) {
        
        WebRtcRoomResponse room = webRtcService.createRoom(sessionId, hostUserId);
        return ApiResponse.success(room, "WebRTC 룸이 생성되었습니다.");
    }

    @Operation(
            summary = "WebRTC 룸 참가",
            description = "WebRTC 룸에 참가합니다."
    )
    @PostMapping("/rooms/{roomId}/join")
    public ApiResponse<WebRtcParticipantResponse> joinRoom(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @RequestBody WebRtcJoinRequest request) {
        
        WebRtcParticipantResponse participant = webRtcService.joinRoom(roomId, request);
        return ApiResponse.success(participant, "WebRTC 룸에 참가했습니다.");
    }

    @Operation(
            summary = "WebRTC 룸 나가기",
            description = "WebRTC 룸에서 나갑니다."
    )
    @PostMapping("/rooms/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam UUID userId) {
        
        webRtcService.leaveRoom(roomId, userId);
        return ApiResponse.success("WebRTC 룸에서 나갔습니다.");
    }

    @Operation(
            summary = "WebRTC 룸 정보 조회",
            description = "WebRTC 룸 정보를 조회합니다."
    )
    @GetMapping("/rooms/{roomId}")
    public ApiResponse<WebRtcRoomResponse> getRoomInfo(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId) {
        
        WebRtcRoomResponse room = webRtcService.getRoomInfo(roomId);
        return ApiResponse.success(room, "WebRTC 룸 정보를 조회했습니다.");
    }

    @Operation(
            summary = "룸 참가자 목록 조회",
            description = "WebRTC 룸의 모든 참가자를 조회합니다."
    )
    @GetMapping("/rooms/{roomId}/participants")
    public ApiResponse<List<WebRtcParticipantResponse>> getRoomParticipants(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId) {
        
        List<WebRtcParticipantResponse> participants = webRtcService.getRoomParticipants(roomId);
        return ApiResponse.success(participants, "참가자 목록을 조회했습니다.");
    }

    @Operation(
            summary = "참가자 상태 업데이트",
            description = "참가자의 카메라/마이크 상태 등을 업데이트합니다."
    )
    @PutMapping("/rooms/{roomId}/participants/{userId}/status")
    public ApiResponse<Void> updateParticipantStatus(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "상태 타입 (camera, microphone, screen_share)", required = true)
            @RequestParam String statusType,
            @Parameter(description = "상태 값", required = true)
            @RequestParam String statusValue) {
        
        webRtcService.updateParticipantStatus(roomId, userId, statusType, statusValue);
        return ApiResponse.success("참가자 상태가 업데이트되었습니다.");
    }

    @Operation(
            summary = "룸 종료",
            description = "WebRTC 룸을 종료합니다. (호스트만 가능)"
    )
    @PostMapping("/rooms/{roomId}/end")
    public ApiResponse<Void> endRoom(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "호스트 사용자 ID", required = true)
            @RequestParam UUID hostUserId) {
        
        webRtcService.endRoom(roomId, hostUserId);
        return ApiResponse.success("WebRTC 룸이 종료되었습니다.");
    }

    @Operation(
            summary = "활성 룸 목록 조회",
            description = "현재 활성화된 WebRTC 룸 목록을 조회합니다."
    )
    @GetMapping("/rooms/active")
    public ApiResponse<List<WebRtcRoomResponse>> getActiveRooms() {
        List<WebRtcRoomResponse> rooms = webRtcService.getActiveRooms();
        return ApiResponse.success(rooms, "활성 룸 목록을 조회했습니다.");
    }

    @Operation(
            summary = "녹화 시작",
            description = "WebRTC 룸의 녹화를 시작합니다."
    )
    @PostMapping("/rooms/{roomId}/recording/start")
    public ApiResponse<Void> startRecording(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam UUID userId) {
        
        webRtcService.startRecording(roomId, userId);
        return ApiResponse.success("녹화가 시작되었습니다.");
    }

    @Operation(
            summary = "녹화 중지",
            description = "WebRTC 룸의 녹화를 중지합니다."
    )
    @PostMapping("/rooms/{roomId}/recording/stop")
    public ApiResponse<Void> stopRecording(
            @Parameter(description = "룸 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam UUID userId) {
        
        webRtcService.stopRecording(roomId, userId);
        return ApiResponse.success("녹화가 중지되었습니다.");
    }

    // WebSocket 메시지 핸들링

    @MessageMapping("/webrtc/{roomId}/signaling")
    public void handleSignaling(@DestinationVariable UUID roomId, 
                               @Payload WebRtcSignalingMessage message,
                               SimpMessageHeaderAccessor accessor) {
        try {
            log.debug("Received signaling message for room {}: {}", roomId, message.getType());
            
            webRtcService.handleSignalingMessage(roomId, message);
            
            // 시그널링 메시지를 대상 피어에게 전달
            String destination = "/topic/webrtc/" + roomId + "/signaling";
            messagingTemplate.convertAndSend(destination, message);
            
        } catch (Exception e) {
            log.error("Error handling signaling message for room {}: {}", roomId, e.getMessage(), e);
            
            // 에러 메시지를 클라이언트에게 전송
            WebRtcSignalingMessage errorMessage = WebRtcSignalingMessage.builder()
                    .type("error")
                    .userId(message.getFromPeerId())
                    .targetUserId(message.getFromPeerId().toString())
                    .data("Signaling error: " + e.getMessage())
                    .build();
            
            String errorDestination = "/topic/webrtc/" + roomId + "/error";
            messagingTemplate.convertAndSend(errorDestination, errorMessage);
        }
    }

    @MessageMapping("/webrtc/{roomId}/participant-update")
    public void handleParticipantUpdate(@DestinationVariable UUID roomId,
                                       @Payload ParticipantUpdateMessage message,
                                       SimpMessageHeaderAccessor accessor) {
        try {
            log.debug("Received participant update for room {}: {} = {}", 
                     roomId, message.getStatusType(), message.getStatusValue());
            
            webRtcService.updateParticipantStatus(roomId, message.getUserId(), 
                                                message.getStatusType(), message.getStatusValue());
            
            // 룸의 모든 참가자에게 업데이트 전달
            String destination = "/topic/webrtc/" + roomId + "/participant-updates";
            messagingTemplate.convertAndSend(destination, message);
            
        } catch (Exception e) {
            log.error("Error handling participant update for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/webrtc/{roomId}/stats")
    public void handleStatsUpdate(@DestinationVariable UUID roomId,
                                 @Payload WebRtcConnectionStatsResponse stats,
                                 SimpMessageHeaderAccessor accessor) {
        try {
            webRtcService.updateConnectionStats(roomId, stats.getFromPeerId().toString(), 
                                              stats.getToPeerId().toString(), stats);
            
            // 통계 정보를 호스트와 모더레이터에게만 전송
            String destination = "/topic/webrtc/" + roomId + "/stats";
            messagingTemplate.convertAndSend(destination, stats);
            
        } catch (Exception e) {
            log.error("Error handling stats update for room {}: {}", roomId, e.getMessage(), e);
        }
    }

    // Helper DTO for participant updates
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParticipantUpdateMessage {
        private UUID userId;
        private String statusType; // camera, microphone, screen_share, etc.
        private Object statusValue;
        private long timestamp = System.currentTimeMillis();
    }
}