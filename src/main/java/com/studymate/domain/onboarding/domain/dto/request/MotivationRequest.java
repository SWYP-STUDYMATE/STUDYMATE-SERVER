package com.studymate.domain.onboarding.domain.dto.request;


import java.util.List;
import java.util.UUID;

public record MotivationRequest (
        UUID userId,
        List<Integer> motivationIds
){

}
