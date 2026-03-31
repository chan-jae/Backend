package com.team.student_calendar.repository;

import com.team.student_calendar.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    // 학생 Task 다 조회 + 완료 처리 안 된 것들
    List<TaskEntity> findAllByStudentEntity_IdAndIsCompletedFalse(Long studentId);
    // 3일 이내 & 기한 초과 Task
    List<TaskEntity> findAllByDueAtIsNotNullAndDueAtLessThanEqualOrderByDueAtAsc(LocalDateTime targetDate);

    // 학생별 7일 이내 ~ 마감 Task
    List<TaskEntity> findAllByStudentEntityIdAndIsCompletedFalseAndDueAtIsNotNullAndDueAtBetweenOrderByDueAtAsc(
            Long studentId, LocalDateTime start, LocalDateTime end);
}