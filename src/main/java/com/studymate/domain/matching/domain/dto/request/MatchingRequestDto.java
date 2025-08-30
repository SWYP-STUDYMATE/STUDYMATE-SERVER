package com.studymate.domain.matching.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchingRequestDto {
    private UUID targetUserId;
    private String message;
}