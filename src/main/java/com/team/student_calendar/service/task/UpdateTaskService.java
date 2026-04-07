package com.team.student_calendar.service.task;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.common.exception.domain.TaskErrorCode;
import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.dto.TaskUpdateReq;
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
@Transactional
public class UpdateTaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    public TaskListRes updateTask(Long taskId, TaskUpdateReq req) {

        log.info("try to update task: {}", taskId);

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BaseException(TaskErrorCode.TASK_NOT_FOUND));

        if (!studentRepository.existsById(task.getStudentEntity().getId())) {
            throw new BaseException(StudentErrorCode.STUDENT_NOT_FOUND);
        }

        task.setContent(req.getContent());
        task.setDueAt(req.getDueAt());

        log.info("success to update task");
        return new TaskListRes(task);
    }

    // 완료
    public void completeTask(Long taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메모가 없습니다. id=" + taskId));

        task.complete();
    }

    // 완료 취소
    @Transactional
    public void cancelTaskCompletion(Long taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메모가 없습니다. id=" + taskId));

        task.incomplete();
    }
}