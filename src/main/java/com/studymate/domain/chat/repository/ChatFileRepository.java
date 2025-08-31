package com.studymate.domain.chat.repository;

import com.studymate.domain.chat.entity.ChatFile;
import com.studymate.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {

    List<ChatFile> findByChatMessageAndIsDeletedFalse(ChatMessage chatMessage);

    List<ChatFile> findByChatMessageIdAndIsDeletedFalse(Long messageId);

    @Query("SELECT cf FROM ChatFile cf WHERE cf.chatMessage.chatRoom.id = :roomId AND cf.isDeleted = false ORDER BY cf.createdAt DESC")
    List<ChatFile> findByRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("roomId") Long roomId);

    @Query("SELECT cf FROM ChatFile cf WHERE cf.chatMessage.chatRoom.id = :roomId AND cf.fileType = :fileType AND cf.isDeleted = false ORDER BY cf.createdAt DESC")
    List<ChatFile> findByRoomIdAndFileTypeAndIsDeletedFalseOrderByCreatedAtDesc(@Param("roomId") Long roomId, @Param("fileType") ChatFile.FileType fileType);

    @Query("SELECT SUM(cf.fileSize) FROM ChatFile cf WHERE cf.chatMessage.chatRoom.id = :roomId AND cf.isDeleted = false")
    Long getTotalFileSizeByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(cf) FROM ChatFile cf WHERE cf.chatMessage.chatRoom.id = :roomId AND cf.isDeleted = false")
    Long countFilesByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT cf FROM ChatFile cf WHERE cf.chatMessage.sender.userId = :userId AND cf.isDeleted = false ORDER BY cf.createdAt DESC")
    List<ChatFile> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("userId") UUID userId);

    // 특정 크기 이상의 파일들 조회 (정리용)
    @Query("SELECT cf FROM ChatFile cf WHERE cf.fileSize > :sizeThreshold AND cf.isDeleted = false")
    List<ChatFile> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);

    // 오래된 파일들 조회 (정리용)
    @Query("SELECT cf FROM ChatFile cf WHERE cf.createdAt < :dateThreshold AND cf.isDeleted = false")
    List<ChatFile> findOldFiles(@Param("dateThreshold") java.time.LocalDateTime dateThreshold);
}