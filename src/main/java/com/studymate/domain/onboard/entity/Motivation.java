package com.studymate.domain.onboard.entity;

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
@Table(name = "MOTIVATION")
public class Motivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOTIVATION_ID")
    private int motivationId;


    @Column (name = "MOTIVATION_NAME")
    private String motivationName;

    @Column(name = "DESCRIPTION")
    private String description;

    // 편의 메서드들
    public String getName() {
        return this.motivationName;
    }
}
