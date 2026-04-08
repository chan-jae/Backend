package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.enums.BookCategory;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.ReadBooksRes;
import com.team.student_calendar.dto.ReadBooksSaveReq;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.StudentBookEntity;
import com.team.student_calendar.service.book.UpdateStudentBookService;
import com.team.student_calendar.service.student.book.DeleteReadBookService;
import com.team.student_calendar.service.student.book.InsertReadBookService;
import com.team.student_calendar.service.student.book.SelectReadBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "학생 읽은 책", description = "StudentBookApiController")
public class StudentBookApiController {

    private final InsertReadBookService insertReadBookService;
    private final SelectReadBookService selectReadBookService;
    private final DeleteReadBookService deleteReadBookService;
    private final UpdateStudentBookService updateStudentBookService;



    @Validated
    @Operation(summary = "읽은 책 저장", description = "학생이 읽은 책을 저장한다.")
    @PostMapping("/api/student-books")
    public ResponseEntity<ApiSuccessResponse<Void>> saveStudentBooks(
            @RequestBody ReadBooksSaveReq req,
            BindingResult bindingResult

    ) {

        log.debug("[StudentBookApiController.saveStudentBooks] List<ReadBooksSaveReq>: {}", req);

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        // 읽은 책 리스트 저장
        StudentBookEntity saved = insertReadBookService.saveReadBookList(req);

        // 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("읽은 책을 성공적으로 저장했습니다: " + saved.getId(),"SUCCESS"));
    }


    @Operation(summary = "읽은 책 가져오기", description = "학생이 읽었던 책들을 모두 가져온다.")
    @GetMapping("/api/student-books/students/{id}")
    public ResponseEntity<ApiSuccessResponse<ReadBooksRes>> selectReadBooks(
            @PathVariable("id") Long studentId,
            @RequestParam(name = "category", required = false, defaultValue = "ALL") String category,
            @PageableDefault(size = 10, page = 0) Pageable pageable
            ) {

        ReadBooksRes res = selectReadBookService
                .findReadBooksByStudentId(studentId, category, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "학생이 읽은 책을 가져왔습니다.", "SUCCESS"));
    }


    @Operation(summary = "학생이 읽은 책 모두 지우기", description = "학생과 관련된 데이터를 모두 제거")
    @DeleteMapping("/api/students/{id}/books")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteReadBooks(
            @PathVariable("id") Long studentId
    ) {

        /* 학생이 읽은책 모두 삭제*/
        deleteReadBookService.deleteReadBooks(studentId);

        /* 응답*/
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("학생이 읽은 책들을 모두 삭제했습니다.", "SUCCESS"));
    }


    @Operation(summary = "학생이 읽은 책 1권 지우기", description = "학생이 읽은 책 1권 지우기")
    @DeleteMapping("/api/students/{studentId}/books/{bookId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteReadBook(
            @PathVariable("studentId") Long studentId,
            @PathVariable("bookId") Long bookId
    ) {

        /* 학생이 읽은책 1권 삭제*/
        deleteReadBookService.deleteReadBook(studentId, bookId);

        /* 응답*/
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("학생이 읽은 책 1권을 삭제했습니다.", "SUCCESS"));
    }

    @Operation(summary = "도서 완료 처리", description = "학생이 읽은 책을 완료 상태(state=1)로 변경합니다.")
    @PatchMapping("/api/students/{studentId}/books/{bookId}/complete")
    public ResponseEntity<ApiSuccessResponse<Void>> completeReadBook(
            @PathVariable("studentId") Long studentId,
            @PathVariable("bookId") Long bookId
    ) {
        updateStudentBookService.completeBook(studentId, bookId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("도서 완료 처리가 되었습니다.", "SUCCESS"));
    }
}
