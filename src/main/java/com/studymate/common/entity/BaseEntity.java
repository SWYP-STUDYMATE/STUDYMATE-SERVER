package com.studymate.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Base entity class with UUID primary key and timestamp fields
 */
@Getter
@MappedSuperclass
public class BaseEntity extends BaseTimeEntity {
    
    @Id
    @Column(length = 36)
    private String id;
    
    protected void setId(String id) {
        this.id = id;
    }
}