package com.team.student_calendar.dto;

import com.team.student_calendar.entity.TaskEntity;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TaskListRes {
    private Long id;
    private String content;
    private LocalDateTime dueAt;
    private LocalDateTime registeredAt;

    public TaskListRes(TaskEntity entity) {
        this.id = entity.getId();
        this.content = entity.getContent();
        this.dueAt = entity.getDueAt();
        this.registeredAt = entity.getRegisteredAt();
    }
}