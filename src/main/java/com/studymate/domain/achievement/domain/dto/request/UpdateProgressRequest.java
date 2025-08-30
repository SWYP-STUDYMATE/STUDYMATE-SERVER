package com.studymate.domain.achievement.domain.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Getter
@NoArgsConstructor
public class UpdateProgressRequest {
    
    @NotBlank(message = "성취 키는 필수입니다.")
    private String achievementKey;
    
    @NotNull(message = "진행도는 필수입니다.")
    @PositiveOrZero(message = "진행도는 0 이상이어야 합니다.")
    private Integer progress;
}