package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookErrorCode implements ErrorCode {

    BOOK_NOT_FOUND("B001", HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다."),
    INVALID_CATEGORY("B002", HttpStatus.BAD_REQUEST, "유효한 카테고리 분류가 아닙니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
