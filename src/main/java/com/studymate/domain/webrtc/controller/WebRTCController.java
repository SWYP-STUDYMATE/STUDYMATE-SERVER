package com.studymate.domain.webrtc.controller;

import com.studymate.common.dto.ApiResponse;
import com.studymate.domain.session.domain.repository.SessionRepository;
import com.studymate.domain.session.entity.Session;
import com.studymate.exception.NotFoundException;
import com.studymate.domain.webrtc.dto.request.WebRTCRoomSyncRequest;
import com.studymate.domain.webrtc.service.WebRTCIntegrationService;
import com.studymate.domain.user.util.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webrtc")
@RequiredArgsConstructor
public class WebRTCController {

    private final WebRTCIntegrationService webRTCIntegrationService;
    private final SessionRepository sessionRepository;

    /**
     * 세션 정보를 기반으로 WebRTC 룸 메타데이터를 동기화합니다.
     */
    @PostMapping("/rooms/{roomId}/sync")
    public ApiResponse<Void> syncRoomMetadata(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String roomId,
            @Valid @RequestBody WebRTCRoomSyncRequest request
    ) {
        UUID requesterId = principal != null ? principal.getUuid() : null;

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new NotFoundException("NOT FOUND SESSION"));

        webRTCIntegrationService.syncRoomWithSession(roomId, requesterId, session);

        return ApiResponse.success(null, "WebRTC 룸 메타데이터를 동기화했습니다.");
    }
}
