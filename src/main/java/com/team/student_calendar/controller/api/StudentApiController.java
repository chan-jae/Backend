package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.FirstLevelReq;
import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.StudentEntity;
import com.team.student_calendar.service.student.DeleteStudentService;
import com.team.student_calendar.service.student.InsertStudentService;
import com.team.student_calendar.service.student.SelectStudentService;
import com.team.student_calendar.service.student.UpdateStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "학생", description = "UserApiController")
public class StudentApiController {

    private final InsertStudentService insertStudentService;
    private final SelectStudentService selectStudentService;
    private final UpdateStudentService updateStudentService;
    private final DeleteStudentService deleteStudentService;


    @Validated
    @Operation(summary = "여러 학생 추가", description = "List 타입 학생들을 추가한다.")
    @PostMapping("/api/students")
    public ResponseEntity<ApiSuccessResponse<UpsertResult>> createStudent(
            @RequestBody List<@Valid StudentCreateReq> studentCreateReqList,
            BindingResult bindingResult
    ) {

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        // 저장
        UpsertResult upsertResult = insertStudentService.saveStudentList(studentCreateReqList);

        // 응답
        if (upsertResult.insertedCount() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiSuccessResponse.created(upsertResult, "학생 추가 및 업데이트에 성공했습니다.", "SUCCESS"));
        }
        else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiSuccessResponse.created(upsertResult, "학생 업데이트에 성공했습니다.", "SUCCESS"));
        }
    }


    @Operation(summary = "전체 학생 가져오기", description = "모든 학생들을 가져온다.")
    @GetMapping("/api/students")
    public ResponseEntity<ApiSuccessResponse<List<StudentEntity>>> selectAllStudents() {

        List<StudentEntity> allStudents = selectStudentService.findAllStudents();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(allStudents, "모든 학생 조회에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "학생 초기레벨 설정", description = "학생 초기레벨을 설정한다.")
    @PatchMapping("/api/students/{id}/first-level")
    public ResponseEntity<ApiSuccessResponse<Void>> updateStudentFirstLevel(
            @PathVariable("id") Long studentId,
            @RequestBody FirstLevelReq req
    ) {

        updateStudentService.updateStudentFirstLevel(studentId, req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("초기 레벨 설정에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "학생 삭제", description = "학생과 관련된 데이터를 모두 지웁니다.")
    @DeleteMapping("/api/students/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteStudent(
        @PathVariable Long id
    ) {

        deleteStudentService.deleteStudent(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("학생 삭제에 성공했습니다.", "SUCCESS"));
    }
}
