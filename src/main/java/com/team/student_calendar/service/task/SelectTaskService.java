package com.team.student_calendar.service.task;

import com.team.student_calendar.dto.TaskListRes;
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
public class SelectTaskService {
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskListRes> getTaskList(Long studentId) {
        return taskRepository.findAllByStudentEntity_Id(studentId).stream()
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
}