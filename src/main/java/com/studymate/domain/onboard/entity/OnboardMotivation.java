package com.studymate.domain.onboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARD_MOTIVATION")
public class OnboardMotivation {

    @EmbeddedId
    private OnboardMotivationId id;

    // 편의 메서드
    public int getMotivationId() {
        return this.id.getMotivationId();
    }
}
