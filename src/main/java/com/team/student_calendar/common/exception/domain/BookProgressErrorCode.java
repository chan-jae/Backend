package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookProgressErrorCode implements ErrorCode {

    BOOK_PROGRESS_NOT_FOUND("BP001", HttpStatus.NOT_FOUND, "책 진행상황을 찾을 수 없습니다.");





    private final String code;
    private final HttpStatus status;
    private final String message;
}
