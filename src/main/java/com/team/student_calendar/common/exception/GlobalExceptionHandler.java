package com.team.student_calendar.common.exception;

import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[GlobalExceptionHandler.handleCustomException] [{}] {} - {}", errorCode.getStatus(), errorCode.getMessage(), e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiErrorResponse.error(errorCode, e.getMessage()));
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("[GlobalExceptionHandler.handleException] Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
