package com.team.student_calendar.dto;

import com.team.student_calendar.entity.TaskEntity;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class TaskCreateRes {
    private Long taskId;
    private String content;
    private LocalDateTime dueAt;
    private LocalDateTime registeredAt;

    public TaskCreateRes(TaskEntity entity) {
        this.taskId = entity.getId();
        this.content = entity.getContent();
        this.dueAt = entity.getDueAt();
        this.registeredAt = entity.getRegisteredAt();
    }
}