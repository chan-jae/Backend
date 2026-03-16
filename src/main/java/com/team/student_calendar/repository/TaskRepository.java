package com.team.student_calendar.repository;

import com.team.student_calendar.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    // 학생 Task 다 조회
    List<TaskEntity> findAllByStudentEntity_Id(Long studentId);
}