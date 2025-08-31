package com.studymate.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelaxedCriteria {
    @Builder.Default
    private Boolean relaxAgeRange = false;
    @Builder.Default
    private Boolean relaxLanguageLevel = false;
    @Builder.Default
    private Boolean relaxLocation = false;
    @Builder.Default
    private Boolean relaxTimeSlots = false;
    @Builder.Default
    private Boolean relaxGender = false;
    @Builder.Default
    private Boolean relaxInterests = false;
    
    public boolean canRelaxAgeRange() {
        return this.relaxAgeRange != null && this.relaxAgeRange;
    }
    
    public boolean canRelaxLanguageLevel() {
        return this.relaxLanguageLevel != null && this.relaxLanguageLevel;
    }
    
    public boolean canRelaxLocation() {
        return this.relaxLocation != null && this.relaxLocation;
    }
    
    public boolean canRelaxTimeSlots() {
        return this.relaxTimeSlots != null && this.relaxTimeSlots;
    }
    
    public boolean canRelaxGender() {
        return this.relaxGender != null && this.relaxGender;
    }
    
    public boolean canRelaxInterests() {
        return this.relaxInterests != null && this.relaxInterests;
    }
}