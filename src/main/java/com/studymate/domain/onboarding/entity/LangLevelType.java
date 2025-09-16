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
@Table(name = "LANG_LEVEL_TYPE")
public class LangLevelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LANG_LEVEL_ID")
    private int langLevelId;

    @Column(name = "LANG_LEVEL_NAME")
    private String langLevelName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY")
    private String category;

    // 편의 메서드들
    public int getLangLevelTypeId() {
        return this.langLevelId;
    }

    public String getName() {
        return this.langLevelName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCategory() {
        return this.category;
    }
}
