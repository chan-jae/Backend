package com.team.student_calendar.service.task;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.entity.TaskEntity;
import com.team.student_calendar.repository.StudentRepository;
import com.team.student_calendar.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsertTaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public TaskCreateRes createTask(Long studentId, TaskCreateReq req) {

        log.info("try to create student: {} task", studentId);

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(StudentErrorCode.STUDENT_NOT_FOUND));

        TaskEntity task = TaskEntity.builder()
                .studentEntity(student)
                .content(req.getContent())
                .dueAt(req.getDueAt())
                .registeredAt(LocalDateTime.now())
                .build();

        TaskEntity saved = taskRepository.save(task);

        log.info("success to create task: {}", saved.getId());

        return new TaskCreateRes(saved);
    }
}