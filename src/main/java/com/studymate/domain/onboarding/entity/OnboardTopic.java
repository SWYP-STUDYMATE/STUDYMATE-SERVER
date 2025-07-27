package com.studymate.domain.onboarding.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ONBOARD_TOPIC")
public class OnboardTopic {

    @EmbeddedId
    private OnboardTopicId id;
}
