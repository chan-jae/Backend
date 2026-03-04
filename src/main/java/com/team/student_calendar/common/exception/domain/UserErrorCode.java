package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");





    private final String code;
    private final HttpStatus status;
    private final String message;
}
