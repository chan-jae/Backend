package com.team.student_calendar.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiSuccessResponse<T> {


    private int status;
    private String result;
    private String message;
    private T data;
    private LocalDateTime time;


    public static <T> ApiSuccessResponse<T> ok(T data, String message, String result) {
        return ApiSuccessResponse.<T>builder()
                .status(HttpStatus.OK.value())  // 200
                .result(result)
                .message(message)
                .data(data)
                .time(LocalDateTime.now())
                .build();
    }

    public static ApiSuccessResponse<Void> ok(String message, String result) {
        return ApiSuccessResponse.<Void>builder()
                .status(HttpStatus.OK.value())  // 200
                .result(result)
                .message(message)
                .data(null)
                .time(LocalDateTime.now())
                .build();
    }

    public static <T> ApiSuccessResponse<T> created(T data, String message, String result) {
        return ApiSuccessResponse.<T>builder()
                .status(HttpStatus.CREATED.value())  // 201
                .result(result)
                .message(message)
                .data(data)
                .time(LocalDateTime.now())
                .build();
    }

    public static ApiSuccessResponse<Void> created(String message, String result) {
        return ApiSuccessResponse.<Void>builder()
                .status(HttpStatus.CREATED.value())  // 201
                .result(result)
                .message(message)
                .data(null)
                .time(LocalDateTime.now())
                .build();
    }
}
