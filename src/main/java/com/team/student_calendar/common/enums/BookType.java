package com.team.student_calendar.common.enums;


import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookType {

    TEACHING_OCEAN((byte) 0),
    CUSTOM((byte) 1);



    private final byte type;



    public static BookType of(String bookType) {

        try {
            return BookType.valueOf(bookType.toUpperCase());
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_TYPE);
        }
    }



    public static void validate(String type) {

        of(type);
    }
}
