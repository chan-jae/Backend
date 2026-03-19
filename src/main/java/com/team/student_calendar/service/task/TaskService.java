package com.team.student_calendar.service.task;

import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.dto.TaskUpdateReq;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.entity.TaskEntity;
import com.team.student_calendar.repository.StudentRepository;
import com.team.student_calendar.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    // 추가
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

    //조회
    @Transactional(readOnly = true)
    public List<TaskListRes> getTaskList(Long studentId) {
        // 레포로 학생 데이터 가져옴
        return taskRepository.findAllByStudentEntity_Id(studentId).stream()
                .map(TaskListRes::new) // 데이터들을 TaskListRes에 넣음
                .toList(); // 리스트로 묶음
    }

    // 삭제
    @Transactional
    public void deleteTask(Long studentId, Long taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 할 일을 찾을 수 없습니다. ID: " + taskId));

        // 삭제 대상 확인
        if (!task.getStudentEntity().getId().equals(studentId)) {
            throw new IllegalArgumentException("해당 학생의 할 일이 아닙니다! (권한 없음)");
        }

        taskRepository.delete(task);
    }

    // 업데이트
    @Transactional
    public TaskListRes updateTask(Long studentId, Long taskId, TaskUpdateReq req) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 할 일을 찾을 수 없습니다. ID: " + taskId));

        if (!task.getStudentEntity().getId().equals(studentId)) {
            throw new IllegalArgumentException("해당 학생의 할 일이 아닙니다! (권한 없음)");
        }

        // JPA 더티 체킹
        // Entity의 @Setter로 값 바꿔치기
        task.setContent(req.getContent());
        task.setDueAt(req.getDueAt());

        return new TaskListRes(task);
    }
}