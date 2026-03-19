package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StudentErrorCode implements ErrorCode {

    STUDENT_NOT_FOUND("S001", HttpStatus.NOT_FOUND, "학생을 찾을 수 없습니다."),
    INVALID_FIRST_LEVEL("S002", HttpStatus.BAD_REQUEST, "올바르지 않은 레벨 입니다.");





    private final String code;
    private final HttpStatus status;
    private final String message;
}
