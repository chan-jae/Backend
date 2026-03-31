package com.team.student_calendar.service.task;

import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.entity.TaskEntity;
import com.team.student_calendar.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelectTaskService {
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskListRes> getTaskList(Long studentId) {
        return taskRepository.findAllByStudentEntity_IdAndIsCompletedFalse(studentId).stream()
                .map(TaskListRes::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<TaskListRes>> getUrgentTasks() {
        LocalDateTime targetDate = LocalDateTime.now().plusDays(3);
        List<TaskListRes> allUrgentTasks = taskRepository.findAllByDueAtIsNotNullAndDueAtLessThanEqualOrderByDueAtAsc(targetDate)
                .stream().map(TaskListRes::new).toList();

        Map<Boolean, List<TaskListRes>> partitionedTasks = allUrgentTasks.stream()
                .collect(Collectors.partitioningBy(TaskListRes::isOverdue));

        return Map.of(
                "overdueTasks", partitionedTasks.get(true),
                "upcomingTasks", partitionedTasks.get(false)
        );
    }

    // 7일 이내 조회
    @Transactional(readOnly = true)
    public List<TaskListRes> getTasksWithin7Days(Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDate = now.plusDays(7);

        List<TaskEntity> urgentTasks = taskRepository
                .findAllByStudentEntityIdAndIsCompletedFalseAndDueAtIsNotNullAndDueAtBetweenOrderByDueAtAsc(
                        studentId, now, targetDate
                );

        return urgentTasks.stream()
                .map(TaskListRes::new)
                .collect(Collectors.toList());
    }
}