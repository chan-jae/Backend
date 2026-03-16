package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TaskApiController {

    private final TaskService taskService;

    // 추가
    @PostMapping("/api/students/{studentId}/tasks")
    public ResponseEntity<ApiSuccessResponse<TaskCreateRes>> createTask(
            @PathVariable("studentId") Long studentId,
            @RequestBody TaskCreateReq req) {

        TaskCreateRes result = taskService.createTask(studentId, req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(result, "할 일(Task) 생성 성공", "SUCCESS"));
    }
}