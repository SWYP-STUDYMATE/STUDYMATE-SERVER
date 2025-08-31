package com.studymate.domain.chat.service;

import com.studymate.domain.chat.dto.response.ChatFileResponse;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ChatFileService {
    
    /**
     * 채팅방에 파일과 함께 메시지 전송
     */
    ChatMessageResponse sendMessageWithFiles(Long roomId, UUID userId, String message, List<MultipartFile> files);
    
    /**
     * 채팅방의 모든 파일 목록 조회
     */
    List<ChatFileResponse> getRoomFiles(Long roomId);
    
    /**
     * 특정 타입의 파일 목록 조회 (이미지, 비디오, 문서 등)
     */
    List<ChatFileResponse> getRoomFilesByType(Long roomId, String fileType);
    
    /**
     * 파일 다운로드 URL 생성
     */
    String generateDownloadUrl(Long fileId);
    
    /**
     * 파일 삭제 (논리 삭제)
     */
    void deleteFile(Long fileId, UUID userId);
    
    /**
     * 사용자가 업로드한 모든 파일 조회
     */
    List<ChatFileResponse> getUserFiles(UUID userId);
    
    /**
     * 채팅방 파일 사용량 통계
     */
    FileUsageStatistics getRoomFileUsage(Long roomId);
    
    /**
     * 파일 저장소 정리 (오래된 파일 삭제)
     */
    void cleanupOldFiles(int daysThreshold);
    
    // 내부 클래스: 파일 사용량 통계
    record FileUsageStatistics(
            long totalFiles,
            long totalSize,
            String displayTotalSize,
            long imageCount,
            long videoCount,
            long audioCount,
            long documentCount,
            long otherCount
    ) {}
}