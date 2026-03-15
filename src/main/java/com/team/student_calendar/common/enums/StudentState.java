package com.team.student_calendar.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentState {

    WAIT(0),
    ACTIVE(1),
    INACTIVE(2);



    private final int state;

}
