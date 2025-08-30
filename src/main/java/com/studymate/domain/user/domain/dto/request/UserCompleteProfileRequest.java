package com.studymate.domain.user.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCompleteProfileRequest {
    private String englishName;
    private String koreanName;
    private String selfBio;
    private String gender;
    private Integer birthYear;
    private String birthday;
    private Integer locationId;
}