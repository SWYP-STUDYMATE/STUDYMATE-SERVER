package com.studymate.domain.onboard.domain.dto.request;

import java.util.List;

public record GroupSizeRequest(
        List<Integer> groupSizeIds
) {
}
