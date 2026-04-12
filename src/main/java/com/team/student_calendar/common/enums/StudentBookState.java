package com.team.student_calendar.common.enums;


import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.ReadBookErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentBookState {

    WAITING((byte) 0),
    READING((byte) 1),
    WRITING((byte) 2),
    DONE((byte) 3);


    private final byte state;




    public static Byte getStateFromString(String stateStr) {

        if (stateStr == null) return null;

        try {
            return StudentBookState.valueOf(stateStr.toUpperCase()).getState();
        } catch (Exception e) {
            throw new BaseException(ReadBookErrorCode.READ_BOOK_INVALID_STATE);
        }
    }
}
