package com.studymate.domain.onboard.entity;

import jakarta.persistence.Column;
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
public class OnboardLearningStyleId implements Serializable {
    @Column(name = "USER_ID")
    private UUID userId;
    
    @Column(name = "LEARNING_STYLE_ID")
    private int learningStyleId;
}
