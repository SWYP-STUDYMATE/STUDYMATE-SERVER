package com.studymate.domain.chat.controller;

import com.studymate.domain.chat.dto.response.ChatFileResponse;
import com.studymate.domain.chat.dto.response.ChatMessageResponse;
import com.studymate.domain.chat.service.ChatFileService;
import com.studymate.domain.user.util.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Tag(name = "Chat Files", description = "채팅 파일 관리 API")
@RestController
@RequestMapping("/api/v1/chat/files")
@RequiredArgsConstructor
public class ChatFileController {

    private final ChatFileService chatFileService;

    @Operation(summary = "파일과 함께 메시지 전송", description = "채팅방에 파일들과 함께 메시지를 전송합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageResponse> uploadFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID") @RequestParam UUID roomId,
            @Parameter(description = "메시지 내용") @RequestParam(required = false) String message,
            @Parameter(description = "업로드할 파일들") @RequestParam("files") List<MultipartFile> files) {

        UUID userId = userDetails.getUserId();
        ChatMessageResponse response = chatFileService.sendMessageWithFiles(roomId, userId, message, files);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅방 파일 목록 조회", description = "특정 채팅방의 모든 파일 목록을 조회합니다.")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ChatFileResponse>> getRoomFiles(
            @Parameter(description = "채팅방 ID") @PathVariable UUID roomId) {
        
        List<ChatFileResponse> files = chatFileService.getRoomFiles(roomId);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "채팅방 특정 타입 파일 조회", description = "특정 채팅방의 특정 타입 파일들을 조회합니다.")
    @GetMapping("/room/{roomId}/type/{fileType}")
    public ResponseEntity<List<ChatFileResponse>> getRoomFilesByType(
            @Parameter(description = "채팅방 ID") @PathVariable UUID roomId,
            @Parameter(description = "파일 타입 (IMAGE, VIDEO, AUDIO, DOCUMENT, ARCHIVE, OTHER)") @PathVariable String fileType) {
        
        List<ChatFileResponse> files = chatFileService.getRoomFilesByType(roomId, fileType);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "내 파일 목록 조회", description = "현재 사용자가 업로드한 모든 파일 목록을 조회합니다.")
    @GetMapping("/my-files")
    public ResponseEntity<List<ChatFileResponse>> getMyFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        UUID userId = userDetails.getUserId();
        List<ChatFileResponse> files = chatFileService.getUserFiles(userId);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "채팅방 파일 사용량 통계", description = "특정 채팅방의 파일 사용량 통계를 조회합니다.")
    @GetMapping("/room/{roomId}/usage")
    public ResponseEntity<ChatFileService.FileUsageStatistics> getRoomFileUsage(
            @Parameter(description = "채팅방 ID") @PathVariable UUID roomId) {
        
        ChatFileService.FileUsageStatistics usage = chatFileService.getRoomFileUsage(roomId);
        return ResponseEntity.ok(usage);
    }

    @Operation(summary = "파일 다운로드", description = "파일을 다운로드합니다.")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId) throws Exception {
        
        // 실제 파일 경로 조회 로직 필요 (보안을 위해 별도 서비스 메서드 구현)
        String downloadUrl = chatFileService.generateDownloadUrl(fileId);
        
        try {
            Path filePath = Paths.get(downloadUrl);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                throw new FileNotFoundException("File not found: " + fileId);
            }
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            throw new FileNotFoundException("File not found: " + fileId);
        }
    }

    @Operation(summary = "파일 삭제", description = "업로드한 파일을 삭제합니다. (논리 삭제)")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "파일 ID") @PathVariable Long fileId) {
        
        UUID userId = userDetails.getUserId();
        chatFileService.deleteFile(fileId, userId);
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "단일 파일 업로드", description = "하나의 파일만 업로드합니다.")
    @PostMapping(value = "/upload-single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatMessageResponse> uploadSingleFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "채팅방 ID") @RequestParam UUID roomId,
            @Parameter(description = "메시지 내용") @RequestParam(required = false) String message,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file) {

        UUID userId = userDetails.getUserId();
        ChatMessageResponse response = chatFileService.sendMessageWithFiles(roomId, userId, message, List.of(file));
        
        return ResponseEntity.ok(response);
    }
}