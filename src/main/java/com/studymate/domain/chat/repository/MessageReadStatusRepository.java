package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.MessageReadStatus;
import com.studymate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    /**
     * 특정 메시지에 대한 사용자의 읽음 상태 조회
     */
    Optional<MessageReadStatus> findByMessageAndReaderAndIsDeletedFalse(ChatMessage message, User reader);

    /**
     * 특정 메시지의 모든 읽음 상태 조회
     */
    @Query("SELECT mrs FROM MessageReadStatus mrs WHERE mrs.message = :message AND mrs.isDeleted = false ORDER BY mrs.readAt ASC")
    List<MessageReadStatus> findByMessageAndIsDeletedFalseOrderByReadAtAsc(@Param("message") ChatMessage message);

    /**
     * 채팅방의 모든 메시지에 대한 사용자의 읽음 상태 조회
     */
    @Query("SELECT mrs FROM MessageReadStatus mrs WHERE mrs.message.chatRoom.id = :roomId AND mrs.reader.userId = :userId AND mrs.isDeleted = false")
    List<MessageReadStatus> findByRoomIdAndUserIdAndIsDeletedFalse(@Param("roomId") Long roomId, @Param("userId") UUID userId);

    /**
     * 사용자의 채팅방별 마지막 읽음 시간 조회
     */
    @Query("SELECT MAX(mrs.readAt) FROM MessageReadStatus mrs WHERE mrs.message.chatRoom.id = :roomId AND mrs.reader.userId = :userId AND mrs.isDeleted = false")
    Optional<LocalDateTime> findLastReadTimeByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") UUID userId);

    /**
     * 채팅방에서 특정 시간 이후의 안읽은 메시지 수 조회
     */
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m 
        WHERE m.chatRoom.id = :roomId 
        AND m.sender.userId != :userId 
        AND m.createdAt > :lastReadTime 
        AND NOT EXISTS (
            SELECT mrs FROM MessageReadStatus mrs 
            WHERE mrs.message = m 
            AND mrs.reader.userId = :userId 
            AND mrs.isDeleted = false
        )
    """)
    long countUnreadMessagesInRoom(@Param("roomId") Long roomId, @Param("userId") UUID userId, @Param("lastReadTime") LocalDateTime lastReadTime);

    /**
     * 사용자의 전체 안읽은 메시지 수 조회
     */
    @Query("""
        SELECT COUNT(DISTINCT m) FROM ChatMessage m
        JOIN m.chatRoom.participants p
        WHERE p.user.userId = :userId
        AND m.sender.userId != :userId
        AND NOT EXISTS (
            SELECT mrs FROM MessageReadStatus mrs
            WHERE mrs.message = m
            AND mrs.reader.userId = :userId
            AND mrs.isDeleted = false
        )
    """)
    long countTotalUnreadMessages(@Param("userId") UUID userId);

    /**
     * 특정 메시지의 읽음 사용자 수 조회
     */
    @Query("SELECT COUNT(mrs) FROM MessageReadStatus mrs WHERE mrs.message = :message AND mrs.isDeleted = false")
    long countReadersByMessage(@Param("message") ChatMessage message);

    /**
     * 채팅방에서 특정 시간 이후 메시지들을 읽음 처리
     */
    @Modifying
    @Query("""
        INSERT INTO MessageReadStatus (message, reader, readAt, isDeleted)
        SELECT m, :reader, :readAt, false
        FROM ChatMessage m
        WHERE m.chatRoom.id = :roomId
        AND m.createdAt <= :readAt
        AND m.sender != :reader
        AND NOT EXISTS (
            SELECT mrs FROM MessageReadStatus mrs
            WHERE mrs.message = m
            AND mrs.reader = :reader
            AND mrs.isDeleted = false
        )
    """)
    void bulkMarkAsRead(@Param("roomId") Long roomId, @Param("reader") User reader, @Param("readAt") LocalDateTime readAt);

    /**
     * 메시지별 읽지 않은 사용자 목록 조회 (채팅방 참가자 중)
     */
    @Query("""
        SELECT p.user FROM ChatRoomParticipant p
        WHERE p.room.id = :roomId
        AND p.user != :sender
        AND NOT EXISTS (
            SELECT mrs FROM MessageReadStatus mrs
            WHERE mrs.message.id = :messageId
            AND mrs.reader = p.user
            AND mrs.isDeleted = false
        )
    """)
    List<User> findUnreadUsersByMessage(@Param("messageId") Long messageId, @Param("roomId") Long roomId, @Param("sender") User sender);

    /**
     * 오래된 읽음 상태 정리 (성능 최적화용)
     */
    @Modifying
    @Query("UPDATE MessageReadStatus mrs SET mrs.isDeleted = true WHERE mrs.createdAt < :cutoffDate AND mrs.isDeleted = false")
    int cleanupOldReadStatuses(@Param("cutoffDate") LocalDateTime cutoffDate);
}