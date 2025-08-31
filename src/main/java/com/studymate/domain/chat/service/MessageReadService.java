package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.response.MessageReadStatusResponse;
import com.studymate.domain.chat.dto.response.UnreadMessageSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageReadService {
    
    /**
     * 메시지를 읽음 처리
     */
    void markMessageAsRead(Long messageId, UUID userId);
    
    /**
     * 채팅방의 모든 메시지를 읽음 처리 (특정 시간까지)
     */
    void markRoomMessagesAsRead(Long roomId, UUID userId, LocalDateTime readUntil);
    
    /**
     * 채팅방의 모든 메시지를 읽음 처리 (현재 시간까지)
     */
    void markAllRoomMessagesAsRead(Long roomId, UUID userId);
    
    /**
     * 특정 메시지의 읽음 상태 조회
     */
    MessageReadStatusResponse getMessageReadStatus(Long messageId);
    
    /**
     * 채팅방별 안읽은 메시지 수 조회
     */
    long getUnreadMessageCount(Long roomId, UUID userId);
    
    /**
     * 사용자의 전체 안읽은 메시지 수 조회
     */
    long getTotalUnreadMessageCount(UUID userId);
    
    /**
     * 사용자의 채팅방별 안읽은 메시지 요약 조회
     */
    List<UnreadMessageSummary> getUnreadMessageSummary(UUID userId);
    
    /**
     * 전체 안읽은 메시지 통계 조회
     */
    UnreadMessageSummary.GlobalUnreadSummary getGlobalUnreadSummary(UUID userId);
    
    /**
     * 사용자의 마지막 읽음 시간 조회
     */
    LocalDateTime getLastReadTime(Long roomId, UUID userId);
    
    /**
     * 메시지가 모든 참가자에게 읽혔는지 확인
     */
    boolean isMessageFullyRead(Long messageId);
    
    /**
     * 읽음 상태 정리 (성능 최적화)
     */
    void cleanupOldReadStatuses(int daysThreshold);
}