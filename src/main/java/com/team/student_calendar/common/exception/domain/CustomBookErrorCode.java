package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomBookErrorCode implements ErrorCode {

    CUSTOM_BOOK_NOT_FOUND("CB001", HttpStatus.NOT_FOUND, "직접 등록한 책을 찾을 수 없습니다."),
    LEVEL_RESOLUTION_FAILED("CB002", HttpStatus.BAD_REQUEST, "해당 난이도와 비교할 기존 책이 없어 레벨을 계산할 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
