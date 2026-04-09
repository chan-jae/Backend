package com.team.student_calendar.service.task;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.common.exception.domain.TaskErrorCode;
import com.team.student_calendar.entity.TaskEntity;
import com.team.student_calendar.repository.StudentRepository;
import com.team.student_calendar.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteTaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public void deleteTask(Long taskId) {

        log.info("try to delete 1 task: {}", taskId);

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BaseException(TaskErrorCode.TASK_NOT_FOUND));

        if (!studentRepository.existsById(task.getStudentEntity().getId())) {
            throw new BaseException(StudentErrorCode.STUDENT_NOT_FOUND);
        }
        taskRepository.delete(task);

        log.info("success to delete 1 task");
    }
}