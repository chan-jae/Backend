package com.team.student_calendar.common.enums;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;

public enum BookCategory {

    ALL, LITERATURE, NON_LITERATURE;

    public static BookCategory of(String category) {
        try {
            return BookCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }
}
