package com.team.student_calendar.common.enums;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;

public enum BookCategory {

    ALL, LITERATURE, NON_LITERATURE;

    public static void validate(String category) {
        try {
            BookCategory parsed = BookCategory.valueOf(category);
            if (parsed == ALL) {
                throw new BaseException(BookErrorCode.INVALID_CATEGORY);
            }
        } catch (IllegalArgumentException e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }
}
