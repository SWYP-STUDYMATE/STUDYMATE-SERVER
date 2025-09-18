package com.studymate.domain.webrtc.service;

import com.studymate.domain.session.domain.repository.SessionRepository;
import com.studymate.domain.session.entity.Session;
import com.studymate.domain.session.type.SessionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebRTCIntegrationService {

    private final RestTemplate workersRestTemplate;
    private final SessionRepository sessionRepository;

    @Value("${workers.api.url:https://workers.languagemate.kr}")
    private String workersApiUrl;

    @Value("${workers.internal.secret:studymate-internal-secret-2024}")
    private String workersInternalSecret;

    /**
     * WebRTC 방 메타데이터를 업데이트합니다.
     */
    public void updateRoomMetadata(String roomId, Map<String, Object> metadata) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        if (CollectionUtils.isEmpty(metadata)) {
            log.debug("WebRTC metadata 업데이트가 생략되었습니다. metadata 비어 있음.");
            return;
        }

        try {
            String url = workersApiUrl + "/api/v1/internal/webrtc/rooms/" + roomId + "/metadata";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", workersInternalSecret);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(metadata, headers);

            workersRestTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            log.info("WebRTC 룸 메타데이터 동기화 완료 - roomId: {}", roomId);
        } catch (RestClientException ex) {
            log.error("WebRTC 메타데이터 동기화 실패 - roomId: {}, error: {}", roomId, ex.getMessage());
        }
    }

    /**
     * 세션 정보를 기반으로 WebRTC 방 메타데이터를 업데이트합니다.
     */
    public void syncRoomWithSession(String roomId, UUID requesterId, Session session) {
        // 호스트 검증
        if (requesterId != null && session.getHostUser() != null
                && !session.getHostUser().getUserId().equals(requesterId)) {
            throw new IllegalArgumentException("세션 호스트만 메타데이터를 동기화할 수 있습니다.");
        }

        // 회의 URL이 비어 있는 경우 roomId를 저장
        if (session.getMeetingUrl() == null || session.getMeetingUrl().isBlank()) {
            session.updateMeetingInfo(roomId, null);
            sessionRepository.save(session);
        }

        Map<String, Object> metadata = buildMetadata(session);
        metadata.put("sessionId", session.getSessionId());
        metadata.put("roomId", roomId);

        updateRoomMetadata(roomId, metadata);
    }

    /**
     * 세션 생성 시 전달받은 roomId가 있다면 즉시 동기화합니다.
     */
    public void syncRoomOnCreation(String roomId, Session session) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        Map<String, Object> metadata = buildMetadata(session);
        metadata.put("sessionId", session.getSessionId());
        metadata.put("roomId", roomId);

        updateRoomMetadata(roomId, metadata);
    }

    private Map<String, Object> buildMetadata(Session session) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", session.getTitle());
        metadata.put("description", session.getDescription());
        Optional.ofNullable(session.getLanguageCode()).ifPresent(code -> metadata.put("language", code));
        Optional.ofNullable(session.getSkillFocus()).ifPresent(focus -> metadata.put("focus", focus));
        Optional.ofNullable(session.getLevelRequirement()).ifPresent(level -> metadata.put("levelRequirement", level));
        Optional.ofNullable(session.getScheduledAt()).ifPresent(time -> metadata.put("scheduledAt", time.toString()));
        Optional.ofNullable(session.getDurationMinutes()).ifPresent(duration -> metadata.put("durationMinutes", duration));
        Optional.ofNullable(session.getTags()).ifPresent(tags -> metadata.put("tags", tags));
        Optional.ofNullable(session.getStatus()).ifPresent(status -> metadata.put("sessionStatus", status.name()));
        Optional.ofNullable(session.getSessionType()).ifPresent(type -> metadata.put("sessionType", mapSessionType(type)));

        if (session.getHostUser() != null) {
            metadata.put("hostUserId", session.getHostUser().getUserId());
            metadata.put("hostName", session.getHostUser().getEnglishName());
            metadata.put("hostAvatar", session.getHostUser().getProfileImage());
        }

        if (session.getGuestUser() != null) {
            metadata.put("guestUserId", session.getGuestUser().getUserId());
            metadata.put("guestName", session.getGuestUser().getEnglishName());
        }

        Optional.ofNullable(session.getMeetingUrl()).ifPresent(url -> metadata.put("meetingUrl", url));

        return metadata;
    }

    private String mapSessionType(SessionType sessionType) {
        if (sessionType == null) {
            return null;
        }

        return switch (sessionType) {
            case VIDEO -> "video";
            case AUDIO -> "audio";
            default -> sessionType.name().toLowerCase();
        };
    }
}
