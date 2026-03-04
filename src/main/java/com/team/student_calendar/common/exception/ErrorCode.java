package com.team.student_calendar.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getStatus(); // 추상 메서드 선언
    String getCode();
    String getMessage();
}
