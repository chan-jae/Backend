package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ErrorCode {

    FILE_NOT_FOUND("F001", HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    UNSUPPORTED_FILE_TYPE("F002", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    EMPTY_FILE("F003", HttpStatus.BAD_REQUEST, "파일이 비어 있습니다."),
    ALREADY_EXISTS_FILE("F004", HttpStatus.CONFLICT, "이미 존재하는 파일이 있습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
