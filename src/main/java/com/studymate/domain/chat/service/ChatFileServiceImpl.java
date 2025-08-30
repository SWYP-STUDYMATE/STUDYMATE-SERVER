package com.studymate.domain.chat.service;

import com.studymate.domain.chat.entity.ChatFile;
import com.studymate.domain.chat.entity.ChatMessage;
import com.studymate.domain.chat.entity.ChatRoom;
import com.studymate.domain.chat.repository.ChatFileRepository;
import com.studymate.domain.chat.repository.ChatMessageRepository;
import com.studymate.domain.chat.repository.ChatRoomRepository;
import com.studymate.domain.chat.dto.response.ChatFileResponse;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatFileServiceImpl implements ChatFileService {

    private final ChatFileRepository chatFileRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    
    @Value("${file.upload.path:/tmp/chat-files}")
    private String uploadPath;
    
    @Value("${file.max-size:10485760}") // 10MB
    private long maxFileSize;
    
    @Value("${file.allowed-types:image/*,video/*,audio/*,application/pdf,application/msword,text/*}")
    private String[] allowedTypes;

    @Override
    public ChatMessageResponse sendMessageWithFiles(UUID roomId, UUID userId, String message, List<MultipartFile> files) {
        // 채팅방과 사용자 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND CHAT ROOM"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND USER"));

        // 메시지 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .message(message)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);

        // 파일들 처리 및 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        ChatFile chatFile = processAndSaveFile(file, chatMessage);
                        chatMessage.addFile(chatFile);
                    } catch (Exception e) {
                        log.error("File upload failed for message {}: {}", chatMessage.getId(), e.getMessage(), e);
                        // 파일 업로드 실패해도 메시지는 보내기 위해 계속 진행
                    }
                }
            }
        }

        return ChatMessageResponse.from(chatMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatFileResponse> getRoomFiles(UUID roomId) {
        List<ChatFile> files = chatFileRepository.findByRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(roomId);
        return files.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatFileResponse> getRoomFilesByType(UUID roomId, String fileType) {
        ChatFile.FileType type;
        try {
            type = ChatFile.FileType.valueOf(fileType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }
        
        List<ChatFile> files = chatFileRepository.findByRoomIdAndFileTypeAndIsDeletedFalseOrderByCreatedAtDesc(roomId, type);
        return files.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(Long fileId) {
        ChatFile file = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND FILE"));
        
        if (file.isDeleted()) {
            throw new IllegalStateException("File has been deleted");
        }
        
        // 실제 환경에서는 CDN이나 signed URL 생성
        return "/api/v1/chat/files/" + fileId + "/download";
    }

    @Override
    public void deleteFile(Long fileId, UUID userId) {
        ChatFile file = chatFileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("NOT FOUND FILE"));
        
        // 파일 업로드한 사용자만 삭제 가능
        if (!file.getChatMessage().getSender().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own files");
        }
        
        file.markAsDeleted();
        chatFileRepository.save(file);
        
        log.info("File marked as deleted: {} by user: {}", fileId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatFileResponse> getUserFiles(UUID userId) {
        List<ChatFile> files = chatFileRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
        return files.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FileUsageStatistics getRoomFileUsage(UUID roomId) {
        List<ChatFile> files = chatFileRepository.findByRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(roomId);
        
        long totalSize = files.stream().mapToLong(ChatFile::getFileSize).sum();
        long imageCount = files.stream().filter(f -> f.getFileType() == ChatFile.FileType.IMAGE).count();
        long videoCount = files.stream().filter(f -> f.getFileType() == ChatFile.FileType.VIDEO).count();
        long audioCount = files.stream().filter(f -> f.getFileType() == ChatFile.FileType.AUDIO).count();
        long documentCount = files.stream().filter(f -> f.getFileType() == ChatFile.FileType.DOCUMENT).count();
        long otherCount = files.size() - (imageCount + videoCount + audioCount + documentCount);
        
        return new FileUsageStatistics(
                files.size(),
                totalSize,
                formatFileSize(totalSize),
                imageCount,
                videoCount,
                audioCount,
                documentCount,
                otherCount
        );
    }

    @Override
    public void cleanupOldFiles(int daysThreshold) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);
        List<ChatFile> oldFiles = chatFileRepository.findOldFiles(cutoffDate);
        
        for (ChatFile file : oldFiles) {
            try {
                // 물리적 파일 삭제
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                
                // 썸네일 삭제
                if (file.getThumbnailUrl() != null) {
                    Path thumbnailPath = Paths.get(file.getThumbnailUrl());
                    if (Files.exists(thumbnailPath)) {
                        Files.delete(thumbnailPath);
                    }
                }
                
                file.markAsDeleted();
                chatFileRepository.save(file);
                
            } catch (IOException e) {
                log.error("Failed to delete old file: {}", file.getFileId(), e);
            }
        }
        
        log.info("Cleaned up {} old files", oldFiles.size());
    }

    // Private helper methods

    private ChatFile processAndSaveFile(MultipartFile file, ChatMessage chatMessage) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        ChatFile.FileType fileType = ChatFile.determineFileType(contentType);
        
        // 파일 저장 경로 생성
        String uploadDir = createUploadDirectory(fileType);
        String storedFilename = generateUniqueFilename(originalFilename);
        Path filePath = Paths.get(uploadDir, storedFilename);
        
        // 파일 저장
        Files.copy(file.getInputStream(), filePath);
        
        // 썸네일 생성 (이미지 파일인 경우)
        String thumbnailUrl = null;
        if (fileType == ChatFile.FileType.IMAGE) {
            thumbnailUrl = generateThumbnail(filePath);
        }
        
        // 파일 URL 생성
        String fileUrl = "/api/v1/chat/files/" + storedFilename;
        
        // ChatFile 엔티티 생성
        ChatFile chatFile = ChatFile.builder()
                .chatMessage(chatMessage)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath.toString())
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(contentType)
                .fileType(fileType)
                .thumbnailUrl(thumbnailUrl)
                .build();
        
        return chatFileRepository.save(chatFile);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds limit: " + formatFileSize(maxFileSize));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedFileType(String contentType) {
        for (String allowedType : allowedTypes) {
            if (allowedType.endsWith("/*")) {
                String prefix = allowedType.substring(0, allowedType.length() - 2);
                if (contentType.startsWith(prefix)) {
                    return true;
                }
            } else if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    private String createUploadDirectory(ChatFile.FileType fileType) throws IOException {
        String typeDir = fileType.name().toLowerCase();
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, typeDir, dateDir);
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        return uploadDir.toString();
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return UUID.randomUUID().toString() + extension;
    }

    private String generateThumbnail(Path imagePath) {
        // 썸네일 생성 로직 구현
        // 실제 환경에서는 ImageIO나 외부 라이브러리 사용
        try {
            String thumbnailFilename = "thumb_" + imagePath.getFileName();
            Path thumbnailPath = imagePath.getParent().resolve(thumbnailFilename);
            
            // 간단한 썸네일 생성 (실제로는 리사이징 로직 필요)
            Files.copy(imagePath, thumbnailPath);
            
            return "/api/v1/chat/files/thumbnails/" + thumbnailFilename;
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail for: {}", imagePath, e);
            return null;
        }
    }

    private ChatFileResponse convertToResponse(ChatFile file) {
        return ChatFileResponse.builder()
                .fileId(file.getFileId())
                .originalFilename(file.getOriginalFilename())
                .fileUrl(file.getFileUrl())
                .downloadUrl(generateDownloadUrl(file.getFileId()))
                .fileSize(file.getFileSize())
                .displaySize(file.getDisplaySize())
                .contentType(file.getContentType())
                .fileType(file.getFileType().name())
                .thumbnailUrl(file.getThumbnailUrl())
                .duration(file.getDuration())
                .fileExtension(file.getFileExtension())
                .previewable(file.isPreviewable())
                .uploadedAt(file.getCreatedAt())
                .build();
    }

    private String formatFileSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long currentSize = size;
        int unitIndex = 0;
        
        while (currentSize >= 1024 && unitIndex < units.length - 1) {
            currentSize /= 1024;
            unitIndex++;
        }
        
        return currentSize + " " + units[unitIndex];
    }
}