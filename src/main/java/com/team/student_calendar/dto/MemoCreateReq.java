package com.team.student_calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

// 프론트에서 받을 내용
@Getter
@NoArgsConstructor
public class MemoCreateReq {
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
}