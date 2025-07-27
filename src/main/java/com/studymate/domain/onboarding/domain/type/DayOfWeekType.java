package com.studymate.domain.onboarding.domain.type;

public enum DayOfWeekType {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    private final String description;

    DayOfWeekType(String description){
        this.description = description;
    }

}
