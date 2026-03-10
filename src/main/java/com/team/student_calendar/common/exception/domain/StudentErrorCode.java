package com.team.student_calendar.common.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StudentErrorCode {

    BOOK_NOT_FOUND("S001", HttpStatus.NOT_FOUND, "학생을 찾을 수 없습니다.");





    private final String code;
    private final HttpStatus status;
    private final String message;
}
