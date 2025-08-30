package com.studymate.domain.session.domain.dto.response;

import com.studymate.domain.session.type.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionBookingResponse {
    private Long bookingId;
    private Long sessionId;
    private String sessionTitle;
    private LocalDateTime sessionScheduledAt;
    private Integer sessionDurationMinutes;
    private String sessionLanguageCode;
    private UUID hostUserId;
    private String hostUserName;
    private String hostUserProfileImage;
    private BookingStatus status;
    private String bookingMessage;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Boolean attended;
    private Integer feedbackRating;
    private String feedbackComment;
    private Boolean reminderSent;
    private Boolean canCancel;
}