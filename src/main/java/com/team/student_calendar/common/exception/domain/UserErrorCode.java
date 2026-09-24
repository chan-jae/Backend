package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_LOGIN_INFO("U001",HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 틀렸습니다."),
    DUPLICATED_NAME("U002",HttpStatus.UNAUTHORIZED, "이미 존재하는 이름 입니다."),
    DUPLICATED_USERNAME("U003",HttpStatus.UNAUTHORIZED, "이미 존재하는 아이디 입니다."),
    USER_NOT_FOUND("U004", HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
