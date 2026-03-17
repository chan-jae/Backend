package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/api/students/{studentId}/tasks")
    public ResponseEntity<ApiSuccessResponse<List<TaskListRes>>> getTasks(
            @PathVariable("studentId") Long studentId) {

        List<TaskListRes> result = taskService.getTaskList(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "할 일 목록 조회 성공", "SUCCESS")
        );
    }
}