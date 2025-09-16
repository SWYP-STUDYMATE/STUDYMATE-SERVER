package com.studymate.domain.onboard.entity;

import com.studymate.domain.onboard.domain.type.PartnerGenderType;
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
@Table(name = "ONBOARD_PARTNER")
public class OnboardPartner {

    @EmbeddedId
    private OnboardPartnerId id;

    // 편의 메서드
    public int getPartnerPersonalityId() {
        return this.id.getPartnerPersonalityId();
    }
}
