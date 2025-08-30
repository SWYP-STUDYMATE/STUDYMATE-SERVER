package com.studymate.domain.session.service;

import com.studymate.domain.session.domain.dto.request.WebRtcJoinRequest;
import com.studymate.domain.session.domain.dto.request.WebRtcSignalingMessage;
import com.studymate.domain.session.domain.dto.response.WebRtcRoomResponse;
import com.studymate.domain.session.domain.dto.response.WebRtcParticipantResponse;
import com.studymate.domain.session.domain.dto.response.WebRtcConnectionStatsResponse;

import java.util.List;
import java.util.UUID;

public interface WebRtcService {

    /**
     * 세션에 대한 WebRTC 룸 생성
     */
    WebRtcRoomResponse createRoom(Long sessionId, UUID hostUserId);

    /**
     * WebRTC 룸 참가
     */
    WebRtcParticipantResponse joinRoom(UUID roomId, WebRtcJoinRequest request);

    /**
     * WebRTC 룸 나가기
     */
    void leaveRoom(UUID roomId, UUID userId);

    /**
     * WebRTC 룸 정보 조회
     */
    WebRtcRoomResponse getRoomInfo(UUID roomId);

    /**
     * 시그널링 메시지 처리 (Offer, Answer, ICE Candidate)
     */
    void handleSignalingMessage(UUID roomId, WebRtcSignalingMessage message);

    /**
     * 룸 내 모든 참가자 조회
     */
    List<WebRtcParticipantResponse> getRoomParticipants(UUID roomId);

    /**
     * 참가자 상태 업데이트 (카메라/마이크 토글, 화면공유 등)
     */
    void updateParticipantStatus(UUID roomId, UUID userId, String statusType, Object statusValue);

    /**
     * 연결 품질 통계 업데이트
     */
    void updateConnectionStats(UUID roomId, String fromPeerId, String toPeerId, 
                             WebRtcConnectionStatsResponse stats);

    /**
     * 룸 종료
     */
    void endRoom(UUID roomId, UUID hostUserId);

    /**
     * 활성 룸 목록 조회
     */
    List<WebRtcRoomResponse> getActiveRooms();

    /**
     * 룸 녹화 시작
     */
    void startRecording(UUID roomId, UUID userId);

    /**
     * 룸 녹화 중지
     */
    void stopRecording(UUID roomId, UUID userId);

    /**
     * 참가자 강제 퇴장 (호스트/모더레이터만)
     */
    void kickParticipant(UUID roomId, UUID hostUserId, UUID targetUserId);

    /**
     * 룸 설정 업데이트 (품질, 기능 활성화 등)
     */
    void updateRoomSettings(UUID roomId, UUID hostUserId, String settingsJson);

    /**
     * 비활성 룸 정리 (스케줄링 작업)
     */
    void cleanupInactiveRooms();

    /**
     * 연결 문제 해결 시도
     */
    void troubleshootConnection(UUID roomId, String peerId);

    /**
     * 룸 통계 조회
     */
    WebRtcConnectionStatsResponse getRoomStats(UUID roomId);
}