package com.team.student_calendar.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentBokState {

    WAITING(0),
    READING(1),
    WRITING(2),
    DONE(3);


    private final int state;
}
