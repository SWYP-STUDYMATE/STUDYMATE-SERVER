package com.studymate.domain.session.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionJoinRequest {
    
    private String message;
    private Map<String, String> userLanguageInfo;
    private String password; // 비공개 세션인 경우
}