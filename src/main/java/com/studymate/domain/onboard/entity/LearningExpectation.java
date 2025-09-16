package com.studymate.domain.onboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "LEARNING_EXPECTATION")
public class LearningExpectation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEARNING_EXPECTATION_ID")
    private Integer learningExpectationId;
    
    @Column(name = "LEARNING_EXPECTATION_NAME", nullable = false)
    private String learningExpectationName;
}