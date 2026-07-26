package com.team.student_calendar.common.enums;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;

public enum BookCategory {

    ALL, LITERATURE, NON_LITERATURE;

    public static BookCategory of(String category) {
        try {
            return BookCategory.valueOf(category.toUpperCase());
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }


    /**
     * 책 생성할 때 카테고리가 유효한지 확인
     * @param category 마테고리
     * @return
     */
    public static void validateForCreate(String category) {

        BookCategory bookCategory = of(category);
        if (bookCategory == ALL) {
            throw new BaseException(BookErrorCode.INVALID_CATEGORY);
        }
    }
}
