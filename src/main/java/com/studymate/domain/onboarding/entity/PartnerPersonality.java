package com.studymate.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "PARTNER_PERSONALITY")
public class PartnerPersonality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARTNER_PERSONALITY_ID")
    private int partnerPersonalityId;

    @Column(name = "PARTNER_PERSONALITY")
    private String partnerPersonality;

    @Column(name = "DESCRIPTION")
    private String description;

    // 편의 메서드들
    public String getName() {
        return this.partnerPersonality;
    }
}
