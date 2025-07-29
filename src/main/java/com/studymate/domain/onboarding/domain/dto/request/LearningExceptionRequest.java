package com.studymate.domain.onboarding.domain.dto.request;

import com.studymate.domain.onboarding.domain.type.LearningExpectionType;


import java.util.UUID;

public record LearningExceptionRequest(
        LearningExpectionType learningExpectionType
) {


}
