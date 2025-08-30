package com.studymate.domain.session.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarResponse {
    private List<CalendarEvent> events;
    private List<AvailableTimeSlot> availableSlots;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CalendarEvent {
        private Long sessionId;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String eventType; // SESSION, BOOKING
        private String status;
        private Boolean isHost;
        private String color; // UI에서 사용할 색상
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableTimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Boolean isAvailable;
    }
}