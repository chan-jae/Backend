package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TaskErrorCode implements ErrorCode {

    TASK_NOT_FOUND("T001", HttpStatus.NOT_FOUND, "해당 할 일을 찾을 수 없습니다."),
    TASK_UNAUTHORIZED("T002", HttpStatus.FORBIDDEN, "해당 학생의 할 일이 아닙니다! (권한 없음)");

    private final String code;
    private final HttpStatus status;
    private final String message;
}