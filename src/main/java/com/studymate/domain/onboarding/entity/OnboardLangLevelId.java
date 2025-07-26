package com.studymate.domain.onboarding.entity;

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
    private UUID usrId;
    private int languageId;
}
