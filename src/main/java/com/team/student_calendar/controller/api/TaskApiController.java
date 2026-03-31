package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.TaskCreateReq;
import com.team.student_calendar.dto.TaskCreateRes;
import com.team.student_calendar.dto.TaskListRes;
import com.team.student_calendar.dto.TaskUpdateReq;
import com.team.student_calendar.service.task.DeleteTaskService;
import com.team.student_calendar.service.task.InsertTaskService;
import com.team.student_calendar.service.task.SelectTaskService;
import com.team.student_calendar.service.task.UpdateTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "메모", description = "TaskApiController")
public class TaskApiController {
    private final InsertTaskService insertTaskService;
    private final SelectTaskService selectTaskService;
    private final UpdateTaskService updateTaskService;
    private final DeleteTaskService deleteTaskService;

    // 추가
    @Operation(summary = "메모 추가", description = "학생별 메모를 추가한다.")
    @PostMapping("/api/students/{studentId}/tasks")
    public ResponseEntity<ApiSuccessResponse<TaskCreateRes>> createTask(
            @PathVariable("studentId") Long studentId,
            @RequestBody TaskCreateReq req) {

        TaskCreateRes result = insertTaskService.createTask(studentId, req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(result, "Task 생성 성공", "SUCCESS"));
    }

    // 조회
    @Operation(summary = "메모 조회", description = "학생별 메모를 조회한다.")
    @GetMapping("/api/students/{studentId}/tasks")
    public ResponseEntity<ApiSuccessResponse<List<TaskListRes>>> getTasks(
            @PathVariable("studentId") Long studentId) {

        List<TaskListRes> result = selectTaskService.getTaskList(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "Task 목록 조회 성공", "SUCCESS")
        );
    }

    // 삭제
    @Operation(summary = "메모 삭제", description = "학생별 메모를 삭제한다.")
    @DeleteMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteTask(
            @PathVariable("taskId") Long taskId) {

        deleteTaskService.deleteTask(taskId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(null, "Task 삭제 완료!", "SUCCESS")
        );
    }

    // 업데이트
    @Operation(summary = "메모 업데이트", description = "학생별 메모를 업데이트한다.")
    @PutMapping("/api/tasks/{taskId}")
    public ResponseEntity<ApiSuccessResponse<TaskListRes>> updateTask(
            @PathVariable("taskId") Long taskId,
            @RequestBody TaskUpdateReq req) {

        TaskListRes result = updateTaskService.updateTask(taskId, req);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "Task 수정 완료!", "SUCCESS")
        );
    }

    // 3일 이내 & 초과
    @Operation(summary = "기한 임박 및 초과 메모 조회", description = "기한을 3일 이내 또는 초과한 메모를 불러온다.")
    @GetMapping("/api/tasks/urgent")
    public ResponseEntity<ApiSuccessResponse<Map<String, List<TaskListRes>>>> getUrgentTasks() {

        Map<String, List<TaskListRes>> result = selectTaskService.getUrgentTasks();

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "기한 임박 및 초과 할 일 목록 조회 성공!", "SUCCESS")
        );
    }

    // 학생별 7일 이내 조회
    @Operation(summary = "학생별 임박한 Task 조회", description = "특정 학생의 7일 이내 마감되는 Task 목록을 조회한다.")
    @GetMapping("/api/students/{studentId}/tasks/upcoming")
    public ResponseEntity<ApiSuccessResponse<List<TaskListRes>>> getUpcomingTasks(
            @PathVariable("studentId") Long studentId) {

        List<TaskListRes> response = selectTaskService.getTasksWithin7Days(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(response, "학생별 7일 이내 Task 조회에 성공했습니다.", "SUCCESS")
        );
    }
    // 완료
    @Operation(summary = "메모 완료 처리", description = "메모를 완료 상태로 변경하여 목록에서 제거한다.")
    @PatchMapping("/api/tasks/{taskId}/complete")
    public ResponseEntity<ApiSuccessResponse<Void>> completeTask(@PathVariable Long taskId) {

        updateTaskService.completeTask(taskId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(null, "Task 완료", "SUCCESS")
        );
    }
}