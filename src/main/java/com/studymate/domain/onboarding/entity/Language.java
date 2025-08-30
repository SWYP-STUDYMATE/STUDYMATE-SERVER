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
@Table(name = "LANGUAGE")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LANGUAGE_ID")
    private int LanguageId;

    @Column(name = "LANGUAGE_NAME")
    private String languageName;

    @Column(name = "LANGUAGE_CODE")
    private String code;


    // 편의 메서드들
    public String getName() {
        return this.languageName;
    }
}
