package com.team.student_calendar.common.enums;


import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentBookState {

    WAITING(0),
    READING(1),
    WRITING(2),
    DONE(3);


    private final int state;




    public static Byte getStateFromString(String stateStr) {

        if (stateStr == null) return null;

        try {
            return (byte) (StudentBookState.valueOf(stateStr.toUpperCase()).getState());
        } catch (Exception e) {
            throw new BaseException(ReadBookErrorCode.READ_BOOK_INVALID_STATE);
        }
    }
}
