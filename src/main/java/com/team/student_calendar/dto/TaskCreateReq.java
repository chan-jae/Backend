package com.team.student_calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskCreateReq {
    private String content;
    private LocalDateTime dueAt; // 언제까지 할 것인가? (선택)
}