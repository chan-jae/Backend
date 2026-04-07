package com.team.student_calendar.controller.api;

import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.BookSliderRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.service.book.InsertBookService;
import com.team.student_calendar.service.book.RecommendBookService;
import com.team.student_calendar.service.book.SearchBookService;
import com.team.student_calendar.service.book.SelectBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
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
    private final SearchBookService searchBookService;
    private final RecommendBookService recommendBookService;



    /**
     * 책 여러개 생성
     *
     * @param bookCreateReqList 책 정보 List
     */
    @Validated
    @Operation(summary = "여러책 삽입", description = "List 타입 책 삽입")
    @PostMapping("/api/books")
    public ResponseEntity<ApiSuccessResponse<Void>> createBook(
            @RequestBody List<@Valid BookCreateReq> bookCreateReqList,
            BindingResult bindingResult) {

        log.info("[BookApiController.createBook] bookCreateReqList: {}", bookCreateReqList);

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        // 책 List 저장
        insertBookService.saveBookList(bookCreateReqList);

        // 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("책 여러권 삽입에 성공했습니다.", "SUCCESS"));
    }
    /**
     * 도서 추천
     */
    @Operation(summary = "도서 추천 조회", description = "학생의 레벨과 문학/비문학 교차를 고려하며 이전/현재/다음 3권의 책을 가져옵니다.")
    @GetMapping("/api/students/{studentId}/books/slider")
    public ResponseEntity<ApiSuccessResponse<List<BookSliderRes>>> getSliderBooks(
            @PathVariable("studentId") Long studentId) {

        log.info("[BookApiController.getSliderBooks] 맞춤 도서 추천 studentId: {}", studentId);

        List<BookSliderRes> result = recommendBookService.getRecommendSliderBooks(studentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(result, "맞춤 도서 조회에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "난이도순으로 책 가져오기", description = "난이도, 제목순으로 책 가져오기")
    @GetMapping("/api/books/by-difficulty")
    public ResponseEntity<ApiSuccessResponse<List<BookEntity>>> selectBookByDifficultyAsc() {

        List<BookEntity> bookEntityList = selectBookService.findAllByDifficultyAscAndTitleAsc();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(bookEntityList, "난이도순으로 책 가져오기에 성공했습니다", "SUCCESS"));
    }

    /**
     * 통합 도서 검색
     */
    @Operation(summary = "통합 도서 검색", description = "제목, 저자, 출판사 키워드로 도서를 검색합니다.")
    @GetMapping("/api/books/search")
    public ResponseEntity<ApiSuccessResponse<List<BookSliderRes>>> searchBooks(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<BookSliderRes> response = searchBookService.searchBooks(keyword);
        return ResponseEntity.ok(ApiSuccessResponse.ok(response, "통합 도서 검색에 성공했습니다.", "SUCCESS"));
    }
}
