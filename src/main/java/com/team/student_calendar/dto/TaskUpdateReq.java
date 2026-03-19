package com.team.student_calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskUpdateReq {
    private String content;      // 바꿀 내용
    private LocalDateTime dueAt; // 바꿀 마감 기한
}