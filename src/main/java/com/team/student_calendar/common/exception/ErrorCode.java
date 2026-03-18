package com.team.student_calendar.common.exception;

import com.team.student_calendar.common.enums.StudentState;
import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
