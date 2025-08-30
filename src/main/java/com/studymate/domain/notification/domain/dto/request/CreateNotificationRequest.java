package com.studymate.domain.notification.domain.dto.request;

import com.studymate.domain.notification.type.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationRequest {
    
    @NotNull(message = "수신자 ID는 필수입니다")
    private UUID userId;
    
    @NotNull(message = "알림 타입은 필수입니다")
    private NotificationType type;
    
    @NotBlank(message = "알림 제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "알림 내용은 필수입니다")
    private String content;
    
    private String actionUrl;
    private String actionData;
    private String imageUrl;
    private String iconUrl;
    private Integer priority = 1; // 1: LOW, 2: NORMAL, 3: HIGH, 4: URGENT
    private String category;
    private LocalDateTime scheduledAt;
    private LocalDateTime expiresAt;
    private Boolean isPersistent = true;
    private String senderUserId;
    private String templateId;
    private String templateVariables;
    private String deliveryChannels = "PUSH"; // PUSH, EMAIL, SMS (콤마 구분)
}