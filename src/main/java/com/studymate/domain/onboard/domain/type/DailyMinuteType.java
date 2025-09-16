package com.studymate.domain.onboard.domain.type;

import lombok.Getter;

@Getter
public enum DailyMinuteType {
    MINUTES_10("10분"),
    MINUTES_15("15분"),
    MINUTES_20("20분"),
    MINUTES_25("25분"),
    MINUTES_30("30분");

    private final String description;

    DailyMinuteType(String description){
        this.description = description;
    }

}
