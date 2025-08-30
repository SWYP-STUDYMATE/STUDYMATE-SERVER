package com.studymate.domain.chat.entity;

import com.studymate.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_files")
public class ChatFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration") // 비디오/오디오 파일의 재생 시간 (초)
    private Integer duration;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum FileType {
        IMAGE,      // 이미지 파일
        VIDEO,      // 비디오 파일
        AUDIO,      // 오디오 파일
        DOCUMENT,   // 문서 파일 (PDF, DOC, etc.)
        ARCHIVE,    // 압축 파일
        OTHER       // 기타 파일
    }

    // Helper methods
    public boolean isImage() {
        return this.fileType == FileType.IMAGE;
    }

    public boolean isVideo() {
        return this.fileType == FileType.VIDEO;
    }

    public boolean isAudio() {
        return this.fileType == FileType.AUDIO;
    }

    public boolean isDocument() {
        return this.fileType == FileType.DOCUMENT;
    }

    public String getFileExtension() {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public String getDisplaySize() {
        if (fileSize == null) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long size = fileSize;
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return size + " " + units[unitIndex];
    }

    public boolean isPreviewable() {
        return isImage() || isVideo() || isDocument();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public static FileType determineFileType(String contentType) {
        if (contentType == null) return FileType.OTHER;
        
        contentType = contentType.toLowerCase();
        
        if (contentType.startsWith("image/")) {
            return FileType.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return FileType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return FileType.AUDIO;
        } else if (contentType.contains("pdf") || 
                   contentType.contains("document") || 
                   contentType.contains("text/") ||
                   contentType.contains("application/msword") ||
                   contentType.contains("application/vnd.openxmlformats-officedocument")) {
            return FileType.DOCUMENT;
        } else if (contentType.contains("zip") || 
                   contentType.contains("rar") ||
                   contentType.contains("tar") ||
                   contentType.contains("7z")) {
            return FileType.ARCHIVE;
        } else {
            return FileType.OTHER;
        }
    }
}