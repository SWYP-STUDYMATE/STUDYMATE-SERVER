package com.studymate.domain.onboard.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARD_GROUP_SIZE")
public class OnboardGroupSize {

    @EmbeddedId
    private OnboardGroupSizeId id;

    // 편의 메서드
    public int getGroupSizeId() {
        return this.id.getGroupSizeId();
    }
}
