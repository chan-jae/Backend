package com.team.student_calendar.service.task;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.StudentErrorCode;
import com.team.student_calendar.common.exception.domain.TaskErrorCode;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final StudentRepository studentRepository;

    // 추가
    @Transactional
    public TaskCreateRes createTask(Long studentId, TaskCreateReq req) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(StudentErrorCode.STUDENT_NOT_FOUND));

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
                .orElseThrow(() -> new BaseException(TaskErrorCode.TASK_NOT_FOUND));

        // 삭제 대상 확인
        if (!task.getStudentEntity().getId().equals(studentId)) {
            throw new BaseException(TaskErrorCode.TASK_UNAUTHORIZED);
        }

        taskRepository.delete(task);
    }

    // 업데이트
    @Transactional
    public TaskListRes updateTask(Long studentId, Long taskId, TaskUpdateReq req) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BaseException(TaskErrorCode.TASK_NOT_FOUND));

        if (!task.getStudentEntity().getId().equals(studentId)) {
            throw new BaseException(TaskErrorCode.TASK_UNAUTHORIZED);
        }

        // JPA 더티 체킹
        // Entity의 @Setter로 값 바꿔치기
        task.setContent(req.getContent());
        task.setDueAt(req.getDueAt());

        return new TaskListRes(task);
    }

    // 모든 학생의 Task 마감 기한 3일 이내 & 기한 초과
    @Transactional(readOnly = true)
    public Map<String, List<TaskListRes>> getUrgentTasks() {

        LocalDateTime targetDate = LocalDateTime.now().plusDays(3); // 3일 이내

        List<TaskListRes> allUrgentTasks = taskRepository.findAllByDueAtIsNotNullAndDueAtLessThanEqualOrderByDueAtAsc(targetDate)
                .stream()
                .map(TaskListRes::new)
                .toList();

        Map<Boolean, List<TaskListRes>> partitionedTasks = allUrgentTasks.stream() // 2파트로 나눔 (기한 3일 이내, 초과)
                .collect(Collectors.partitioningBy(TaskListRes::isOverdue));

        return Map.of(
                "overdueTasks", partitionedTasks.get(true),   // 기한 지난 메모들 (isOverdue == true)
                "upcomingTasks", partitionedTasks.get(false)  // 3일 이내 메모들 (isOverdue == false)
        );
    }
}