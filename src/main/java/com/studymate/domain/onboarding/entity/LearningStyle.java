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
@Table(name = "LEARNING_STYLE")
public class LearningStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEARNING_STYLE_ID")
    private int learningStyleId;

    @Column(name = "LEARNING_STYLE_NAME")
    private String learningStyleName;


}
