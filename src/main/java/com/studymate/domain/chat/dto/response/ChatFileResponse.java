package com.studymate.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileResponse {
    
    private Long fileId;
    
    private String originalFilename;
    
    private String fileUrl;
    
    private String downloadUrl;
    
    private Long fileSize;
    
    private String displaySize;
    
    private String contentType;
    
    private String fileType; // IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, OTHER
    
    private String thumbnailUrl;
    
    private Integer duration; // 비디오/오디오 재생 시간 (초)
    
    private String fileExtension;
    
    private boolean previewable;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadedAt;
    
    // Helper methods
    public boolean isImage() {
        return "IMAGE".equals(fileType);
    }
    
    public boolean isVideo() {
        return "VIDEO".equals(fileType);
    }
    
    public boolean isAudio() {
        return "AUDIO".equals(fileType);
    }
    
    public boolean isDocument() {
        return "DOCUMENT".equals(fileType);
    }
}