package com.team.student_calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReadBooksSaveReq {

    @NotNull(message = "학생 id 값은 필수입니다.")
    private Long studentId;

    @NotNull(message = "책 id 값은 필수입니다.")
    private Long bookId;

    @NotBlank(message = "상태 문자열 값은 필수입니다.")
    private String stateStr;
}
