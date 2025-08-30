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
@Table(name = "ONBOARD_TOPIC")
public class OnboardTopic {

    @EmbeddedId
    private OnboardTopicId id;

    @Column(name = "TOPIC_NAME")
    private String topicName;

    // 편의 메서드
    public int getTopicId() {
        return this.id.getTopicId();
    }

    public String getTopicName() {
        return this.topicName;
    }
}
