package com.team.student_calendar.dto;

import com.team.student_calendar.entity.TaskEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskListRes {
    private Long id;

    private Long studentId;
    private String studentName;

    private String content;
    private LocalDateTime dueAt;
    private LocalDateTime registeredAt;

    private boolean isOverdue; // 기간 초과

    public TaskListRes(TaskEntity entity) {
        this.id = entity.getId();

        this.studentId = entity.getStudentEntity().getId();
        this.studentName = entity.getStudentEntity().getName();

        this.content = entity.getContent();
        this.dueAt = entity.getDueAt();
        this.registeredAt = entity.getRegisteredAt();

        // now보다 isBefore이면 초과
        this.isOverdue = entity.getDueAt() != null && entity.getDueAt().isBefore(LocalDateTime.now());
    }
}