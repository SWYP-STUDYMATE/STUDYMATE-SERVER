package com.studymate.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 온보딩 성격 매핑 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARDING_PERSONALITY")
public class OnboardingPersonality {

    @EmbeddedId
    private OnboardPartnerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSONALITY_TYPE_ID", insertable = false, updatable = false)
    private PartnerPersonality partnerPersonality;

    // 편의 메서드들
    public int getPartnerPersonalityId() {
        return this.id.getPartnerPersonalityId();
    }

    public String getPersonalityType() {
        return this.partnerPersonality != null ? this.partnerPersonality.getName() : null;
    }
}