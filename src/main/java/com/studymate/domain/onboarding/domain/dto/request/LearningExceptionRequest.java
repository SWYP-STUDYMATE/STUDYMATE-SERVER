package com.studymate.domain.onboarding.domain.dto.request;

import java.util.List;

public record LearningExceptionRequest(
        List<Integer> learningExpectationIds
) {


}
