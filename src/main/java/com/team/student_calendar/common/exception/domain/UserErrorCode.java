package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_LOGIN_INFO("U001",HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 틀렸습니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
