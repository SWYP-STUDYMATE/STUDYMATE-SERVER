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
@Table(name = "LANG_LEVEl_TYPE")
public class LangLevelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LANG_LEVEL_ID")
    private int langLevelId;

    @Column(name = "LANG_LEVEL_NAME")
    private String langLevelName;


}
