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
@Table(name = "GROUP_SIZE")
public class GroupSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_SIZE_ID")
    private int groupSizeId;

    @Column(name = "GROUP_SIZE")
    private String groupSize;

    @Column(name = "DESCRIPTION")
    private String description;

    // 편의 메서드들
    public String getName() {
        return this.groupSize;
    }
}
