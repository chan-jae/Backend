package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JWTException implements ErrorCode {

    INVALID_REFRESH_TOKEN("J001", HttpStatus.BAD_REQUEST, "유효한 리프레시 토큰이 아닙니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
