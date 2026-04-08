package com.team.student_calendar.controller.api;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.service.book.InsertBookService;
import com.team.student_calendar.service.book.SelectBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "책", description = "BookApiController")

public class BookApiController {

    private final InsertBookService insertBookService;
    private final SelectBookService selectBookService;

    /**
     * 책 여러개 생성
     *
     * @param bookCreateReqList 책 정보 List
     */
    @Validated
    @Operation(summary = "여러책 삽입", description = "List 타입 책 삽입")
    @PostMapping("/api/books")
    public ResponseEntity<ApiSuccessResponse<UpsertResult>> createBook(
            @RequestBody List<@Valid BookCreateReq> bookCreateReqList,
            BindingResult bindingResult) {

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        // 책 List 저장
        UpsertResult upsertResult = insertBookService.saveBookList(bookCreateReqList);
        // 삽입된 데이터가 없으면 200 응답
        if (upsertResult.insertedCount() == 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiSuccessResponse.created(upsertResult, "책 리스트가 없거나 모두 중복된 책입니다.", "SUCCESS"));
        }
        // 삽입된 데이터가 있으면 201 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(upsertResult, "책 여러권 삽입에 성공했습니다.", "SUCCESS"));
    }

    /**
     * 책 목록 페이징 조회
     *
     * @param page 조회 할 페이지 번호 (기본값: 1)
     * @param size 한 페이지 책 개수 (기본값: 10)
     */
    @Operation(summary = "책 목록 페이징 조회", description = "페이지 번호와 사이즈를 입력하여 책 목록을 조회합니다.")
    @GetMapping("/api/books")
    public ResponseEntity<ApiSuccessResponse<Page<BookEntity>>> getBookList(
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size) {

        // 서비스에서 Page 가져옴
        Page<BookEntity> bookPage = selectBookService.getBookListWithPaging(page, size);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(bookPage, "책 목록 페이징 조회에 성공했습니다.", "SUCCESS"));
    }

    /**
     * 슬라이더로 도서 조회
     */
    @Operation(summary = "도서 조회", description = "학생의 현재 진도에 맞춰 자동으로 중앙이 세팅된 책 5권을 반환합니다.")
    @GetMapping("/api/students/{studentId}/books/slider")
    public ResponseEntity<ApiSuccessResponse<List<BookSliderRes>>> getSliderBooks(
            @PathVariable("studentId") Long studentId) {

        List<BookSliderRes> result = selectBookService.getSliderBooks(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "도서 조회에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "난이도순으로 책 가져오기", description = "난이도, 제목순으로 책 가져오기")
    @GetMapping("/api/books/by-difficulty")
    public ResponseEntity<ApiSuccessResponse<List<BookEntity>>> selectBookByDifficultyAsc() {

        List<BookEntity> bookEntityList = selectBookService.findAllByDifficultyAscAndTitleAsc();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(bookEntityList, "난이도순으로 책 가져오기에 성공했습니다", "SUCCESS"));
    }
}
