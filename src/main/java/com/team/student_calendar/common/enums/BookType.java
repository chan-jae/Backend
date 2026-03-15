package com.team.student_calendar.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookType {

    TEACHING_OCEAN(0),
    CUSTOM(1);



    private final int type;
}
