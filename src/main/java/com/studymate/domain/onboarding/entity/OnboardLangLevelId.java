package com.studymate.domain.onboarding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OnboardLangLevelId {
    @Column(name = "USER_ID")
    private UUID userId;
    
    @Column(name = "LANG_ID")
    private int languageId;
}
