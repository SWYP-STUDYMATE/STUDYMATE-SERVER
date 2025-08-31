package com.studymate.domain.session.domain.dto.request;

import lombok.Data;

@Data
public class JoinGroupSessionRequest {
    
    private String joinCode;
    
    private String message;
    
    private Boolean acceptsTerms = true;
}