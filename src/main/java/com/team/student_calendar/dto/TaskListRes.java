package com.team.student_calendar.dto;

import com.team.student_calendar.entity.TaskEntity;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class TaskListRes {
    private Long id;

    private Long studentId;
    private String studentName;

    private String content;
    private LocalDateTime dueAt;
    private LocalDateTime registeredAt;

    private String dDay;       // 디데이 텍스트 ("D-3" or "기한 초과")
    private boolean isOverdue; // 기간 초과 스위치

    public TaskListRes(TaskEntity entity) {
        this.id = entity.getId();

        this.studentId = entity.getStudentEntity().getId();
        this.studentName = entity.getStudentEntity().getName();

        this.content = entity.getContent();
        this.dueAt = entity.getDueAt();
        this.registeredAt = entity.getRegisteredAt();

        // D-Day 계산 및 초과 여부 판별
        this.dDay = calculateDDay(entity.getDueAt());

        // 마감일 초과 (now보다 isBefore이면 true)
        this.isOverdue = entity.getDueAt() != null && entity.getDueAt().isBefore(LocalDateTime.now());
    }

    // D-Day 계산기
    private String calculateDDay(LocalDateTime dueAt) {
        if (dueAt == null) {
            return "기한 없음";
        }

        LocalDate today = LocalDate.now();
        LocalDate targetDate = dueAt.toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(today, targetDate);

        if (daysBetween > 0) {
            return "D-" + daysBetween;
        } else if (daysBetween == 0) {
            return "D-Day";
        } else {
            return "D+" + Math.abs(daysBetween);
        }
    }
}