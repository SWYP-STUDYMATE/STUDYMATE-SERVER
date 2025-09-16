package com.studymate.domain.onboarding.entity;

import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
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
@Table(name = "ONBOARDING_PARTNER")
public class OnboardingPartner {

    @EmbeddedId
    private OnboardingPartnerId id;

    // 편의 메서드
    public int getPartnerPersonalityId() {
        return this.id.getPartnerPersonalityId();
    }
}
