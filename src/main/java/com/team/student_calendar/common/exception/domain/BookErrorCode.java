package com.team.student_calendar.common.exception.domain;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookErrorCode implements ErrorCode {

    BOOK_NOT_FOUND("B001", HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다."),
    INVALID_CATEGORY("B002", HttpStatus.BAD_REQUEST, "유효한 카테고리 분류가 아닙니다."),
    INVALID_LEVEL("B003", HttpStatus.BAD_REQUEST, "유효한 레벨이 아닙니다. (A_0~A_9, B_0~B_9)"),
    DIFFICULTY_OUT_OF_RANGE("B004", HttpStatus.BAD_REQUEST, "난이도가 선택한 레벨의 범위를 벗어났습니다."),
    INVALID_TYPE("B005", HttpStatus.BAD_REQUEST, "유효한 책 타입이 아닙니다."),
    INVALID_EXCEL_FILE("B006", HttpStatus.BAD_REQUEST, "유효한 책 정보가 담긴 액셀 파일이 아닙니다."),
    TOO_MANY_EXCEL_DATA("B007", HttpStatus.BAD_REQUEST, "액셀에 데이터가 너무 많이 있습니다."),
    ALREADY_EXIST_BOOK("B008", HttpStatus.BAD_REQUEST, "이미 등록된 책 입니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
