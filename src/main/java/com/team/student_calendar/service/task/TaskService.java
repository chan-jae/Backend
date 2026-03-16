package com.team.student_calendar.service.task;

import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.entity.TaskEntity;
import com.team.student_calendar.repository.StudentRepository;
import com.team.student_calendar.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public TaskCreateRes createTask(Long studentId, TaskCreateReq req) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생을 찾을 수 없습니다: " + studentId));

        TaskEntity task = TaskEntity.builder()
                .studentEntity(student)
                .content(req.getContent())
                .dueAt(req.getDueAt())
                .registeredAt(LocalDateTime.now()) // 등록 시간은 현재 시간으로 강제 주입!
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        return new TaskCreateRes(savedTask);
    }
}