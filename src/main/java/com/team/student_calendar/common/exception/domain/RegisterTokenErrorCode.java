package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RegisterTokenErrorCode implements ErrorCode {

    INVALID_REGISTER_TOKEN("RT001", HttpStatus.UNAUTHORIZED, "유효하지 않은 가입 토큰입니다."),
    EXPIRED_REGISTER_TOKEN("RT002", HttpStatus.UNAUTHORIZED, "만료된 가입 토큰입니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
