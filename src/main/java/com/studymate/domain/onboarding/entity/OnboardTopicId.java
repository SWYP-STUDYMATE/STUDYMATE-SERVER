package com.studymate.domain.onboarding.entity;

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
public class OnboardTopicId implements Serializable {
    @Column(name = "USER_ID")
    private UUID userId;
    
    @Column(name = "TOPIC_ID")
    private int topicId;
}
