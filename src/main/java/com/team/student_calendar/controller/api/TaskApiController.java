package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.dto.TaskUpdateReq;
import com.team.student_calendar.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    //조회
    @GetMapping("/api/students/{studentId}/tasks")
    public ResponseEntity<ApiSuccessResponse<List<TaskListRes>>> getTasks(
            @PathVariable("studentId") Long studentId) {

        List<TaskListRes> result = taskService.getTaskList(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "할 일 목록 조회 성공", "SUCCESS")
        );
    }

    // 삭제
    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteTask(
            @PathVariable("taskId") Long taskId) {

        taskService.deleteTask(taskId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(null, "할 일(Task) 삭제 완료!", "SUCCESS")
        );
    }

    // 업데이트
    @PutMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiSuccessResponse<TaskListRes>> updateTask(
            @PathVariable("taskId") Long taskId,
            @RequestBody TaskUpdateReq req) {

        TaskListRes result = taskService.updateTask(taskId, req);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "할 일(Task) 수정 완료!", "SUCCESS")
        );
    }

    // 3일 이내 & 초과
    @GetMapping("/api/tasks/urgent")
    public ResponseEntity<ApiSuccessResponse<Map<String, List<TaskListRes>>>> getUrgentTasks() {

        Map<String, List<TaskListRes>> result = taskService.getUrgentTasks();

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "기한 3일 이내 및 초과 할 일 목록 조회 성공!", "SUCCESS")
        );
    }
}