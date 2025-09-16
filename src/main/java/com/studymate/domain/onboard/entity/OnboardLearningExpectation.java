package com.studymate.domain.onboard.entity;

import com.studymate.domain.user.entity.User;
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
@Table(name = "ONBOARD_LEARNING_EXPECTATION")
public class OnboardLearningExpectation {

    @EmbeddedId
    private OnboardLearningExpectationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEARNING_EXPECTATION_ID", insertable = false, updatable = false)
    private LearningExpectation learningExpectation;
}