package com.studymate.domain.onboarding.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OnboardTopicId implements Serializable {
    private UUID userId;
    private int topicId;
}
