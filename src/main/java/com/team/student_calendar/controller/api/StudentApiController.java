package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.StudentCreateReq;
import com.team.student_calendar.service.student.InsertStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "학생", description = "UserApiController")
public class StudentApiController {

    private final InsertStudentService insertStudentService;


    @Validated
    @Operation(summary = "여러 학생 추가", description = "List 타입 학생들을 추가한다.")
    @PostMapping("/api/students")
    public ResponseEntity<ApiSuccessResponse<Void>> createStudent(
            @RequestBody List<@Valid StudentCreateReq> studentCreateReqList,
            BindingResult bindingResult
    ) {

        log.info("[StudentApiController.createStudent] 요청 건수: {}", studentCreateReqList.size());

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        // 저장
        boolean isInserted = insertStudentService.saveStudentList(studentCreateReqList);

        // 응답
        if (isInserted) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiSuccessResponse.created("학생 리스트 추가 및 수정에 성공했습니다.", "SUCCESS"));
        }
        else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiSuccessResponse.created("학생 수정에 성공했습니다.", "SUCCESS"));
        }
    }
}
