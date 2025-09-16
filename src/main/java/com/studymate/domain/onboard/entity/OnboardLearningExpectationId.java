package com.studymate.domain.onboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class OnboardLearningExpectationId implements Serializable {

    @Column(name = "USER_ID")
    private UUID userId;

    @Column(name = "LEARNING_EXPECTATION_ID")
    private Integer learningExpectationId;
}