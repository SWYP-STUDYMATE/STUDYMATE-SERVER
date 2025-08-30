package com.studymate.domain.chat.controller;

import com.studymate.domain.chat.dto.response.MessageReadStatusResponse;
import com.studymate.domain.chat.dto.response.UnreadMessageSummary;
import com.studymate.domain.chat.service.MessageReadService;
import com.studymate.common.dto.response.ApiResponse;
import com.studymate.auth.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/read-status")
@Tag(name = "Message Read Status", description = "메시지 읽음 상태 관리 API")
public class MessageReadController {

    private final MessageReadService messageReadService;
    private final JwtUtils jwtUtils;

    @Operation(
            summary = "메시지 읽음 처리",
            description = "특정 메시지를 읽음 상태로 마킹합니다."
    )
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(
            @Parameter(description = "메시지 ID", required = true)
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        messageReadService.markMessageAsRead(messageId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("메시지 읽음 처리가 완료되었습니다."));
    }

    @Operation(
            summary = "채팅방 메시지 읽음 처리 (특정 시간까지)",
            description = "채팅방의 특정 시간까지의 모든 메시지를 읽음 상태로 마킹합니다."
    )
    @PostMapping("/rooms/{roomId}/read-until")
    public ResponseEntity<ApiResponse<Void>> markRoomMessagesAsRead(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable UUID roomId,
            @Parameter(description = "읽음 처리할 시간", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime readUntil,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        messageReadService.markRoomMessagesAsRead(roomId, userId, readUntil);
        
        return ResponseEntity.ok(ApiResponse.success("채팅방 메시지 읽음 처리가 완료되었습니다."));
    }

    @Operation(
            summary = "채팅방 전체 메시지 읽음 처리",
            description = "채팅방의 현재 시간까지 모든 메시지를 읽음 상태로 마킹합니다."
    )
    @PostMapping("/rooms/{roomId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRoomMessagesAsRead(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable UUID roomId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        messageReadService.markAllRoomMessagesAsRead(roomId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("채팅방 전체 메시지 읽음 처리가 완료되었습니다."));
    }

    @Operation(
            summary = "메시지 읽음 상태 조회",
            description = "특정 메시지의 상세 읽음 상태를 조회합니다."
    )
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<MessageReadStatusResponse>> getMessageReadStatus(
            @Parameter(description = "메시지 ID", required = true)
            @PathVariable Long messageId) {
        
        MessageReadStatusResponse response = messageReadService.getMessageReadStatus(messageId);
        return ResponseEntity.ok(ApiResponse.success(response, "메시지 읽음 상태 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "채팅방 안읽은 메시지 수 조회",
            description = "특정 채팅방의 안읽은 메시지 개수를 조회합니다."
    )
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable UUID roomId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        long unreadCount = messageReadService.getUnreadMessageCount(roomId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(unreadCount, "안읽은 메시지 수 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "전체 안읽은 메시지 수 조회",
            description = "사용자의 모든 채팅방에서 안읽은 메시지 총 개수를 조회합니다."
    )
    @GetMapping("/total-unread-count")
    public ResponseEntity<ApiResponse<Long>> getTotalUnreadMessageCount(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        long totalUnreadCount = messageReadService.getTotalUnreadMessageCount(userId);
        
        return ResponseEntity.ok(ApiResponse.success(totalUnreadCount, "전체 안읽은 메시지 수 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "채팅방별 안읽은 메시지 요약 조회",
            description = "사용자의 모든 채팅방별 안읽은 메시지 요약 정보를 조회합니다."
    )
    @GetMapping("/unread-summary")
    public ResponseEntity<ApiResponse<List<UnreadMessageSummary>>> getUnreadMessageSummary(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        List<UnreadMessageSummary> summaries = messageReadService.getUnreadMessageSummary(userId);
        
        return ResponseEntity.ok(ApiResponse.success(summaries, "안읽은 메시지 요약 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "전체 안읽은 메시지 통계 조회",
            description = "사용자의 전체 안읽은 메시지 통계 정보를 조회합니다."
    )
    @GetMapping("/global-unread-summary")
    public ResponseEntity<ApiResponse<UnreadMessageSummary.GlobalUnreadSummary>> getGlobalUnreadSummary(
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        UnreadMessageSummary.GlobalUnreadSummary summary = messageReadService.getGlobalUnreadSummary(userId);
        
        return ResponseEntity.ok(ApiResponse.success(summary, "전체 안읽은 메시지 통계 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "마지막 읽음 시간 조회",
            description = "사용자의 특정 채팅방에서 마지막 읽음 시간을 조회합니다."
    )
    @GetMapping("/rooms/{roomId}/last-read-time")
    public ResponseEntity<ApiResponse<LocalDateTime>> getLastReadTime(
            @Parameter(description = "채팅방 ID", required = true)
            @PathVariable UUID roomId,
            @RequestHeader("Authorization") String token) {
        
        UUID userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        LocalDateTime lastReadTime = messageReadService.getLastReadTime(roomId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(lastReadTime, "마지막 읽음 시간 조회가 완료되었습니다."));
    }

    @Operation(
            summary = "메시지 완전 읽음 여부 확인",
            description = "특정 메시지가 모든 참가자에게 읽혔는지 확인합니다."
    )
    @GetMapping("/messages/{messageId}/fully-read")
    public ResponseEntity<ApiResponse<Boolean>> isMessageFullyRead(
            @Parameter(description = "메시지 ID", required = true)
            @PathVariable Long messageId) {
        
        boolean isFullyRead = messageReadService.isMessageFullyRead(messageId);
        return ResponseEntity.ok(ApiResponse.success(isFullyRead, "메시지 완전 읽음 여부 확인이 완료되었습니다."));
    }

    @Operation(
            summary = "오래된 읽음 상태 정리",
            description = "성능 최적화를 위해 지정된 일수보다 오래된 읽음 상태를 정리합니다. (관리자 전용)"
    )
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupOldReadStatuses(
            @Parameter(description = "정리할 기준 일수", required = true)
            @RequestParam int daysThreshold) {
        
        messageReadService.cleanupOldReadStatuses(daysThreshold);
        return ResponseEntity.ok(ApiResponse.success("읽음 상태 정리가 완료되었습니다."));
    }
}