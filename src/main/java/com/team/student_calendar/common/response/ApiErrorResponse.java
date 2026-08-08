package com.team.student_calendar.common.response;

import com.team.student_calendar.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {

    private int status;
    private String message;
    private String errorCode;
    private LocalDateTime time;



    public static ApiErrorResponse error(ErrorCode errorCode) {
        return ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .message(errorCode.getMessage())
                .errorCode(errorCode.getCode())
                .time(LocalDateTime.now())
                .build();
    }

    public static ApiErrorResponse error(ErrorCode errorCode, String message) {
        return ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .message(message)
                .errorCode(errorCode.getCode())
                .time(LocalDateTime.now())
                .build();
    }
}
