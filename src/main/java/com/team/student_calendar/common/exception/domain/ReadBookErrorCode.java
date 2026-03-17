package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReadBookErrorCode implements ErrorCode {

    READ_BOOK_NOT_FOUND("RB001", HttpStatus.NOT_FOUND, "읽은 책을 찾을 수 없습니다."),
    READ_BOOK_INVALID_STATE("RB002", HttpStatus.BAD_REQUEST, "읽은 책이 가질 수 상태가 유효하지 않습니다."),
    ALREADY_READ_BOOK("RB003", HttpStatus.CONFLICT, "이미 읽은 책이 포함되어 있습니다.");







    private final String code;
    private final HttpStatus status;
    private final String message;
}
