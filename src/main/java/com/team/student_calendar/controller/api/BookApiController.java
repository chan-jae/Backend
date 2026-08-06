package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.enums.LevelDifficultyRange;
import com.team.student_calendar.dto.BookCreateReq;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.ManualBookDto;
import com.team.student_calendar.dto.RecBookRes;
import com.team.student_calendar.dto.UpsertResult;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.service.book.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "책", description = "BookApiController")

public class BookApiController {

    private final InsertBookService insertBookService;
    private final SelectBookService selectBookService;
//    private final SearchBookService searchBookService;
    private final RecommendBookService recommendBookService;
    private final UpdateBookService updateBookService;
    private final DeleteBookService deleteBookService;






    @Validated
    @Operation(summary = "책 레벨 난이도 범위 가져오기", description = "레벨에 해당하는 난이도 범위 반환")
    @GetMapping("/api/books/difficulty-range")
    public ResponseEntity<ApiSuccessResponse<Map<String, LevelDifficultyRange>>> getDifficultyRange() {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(LevelDifficultyRange.asMap(), "레벨별 난이도 범위 조회에 성공했습니다.", "SUCCESS"));
    }


    @Validated
    @Operation(summary = "책 1권 수동 등록", description = "자동 등록이 아닌 수동 등록")
    @PostMapping("/api/books/manual")
    public ResponseEntity<ApiSuccessResponse<Void>> selfCreateBook(
            @RequestBody @Valid ManualBookDto req,
            BindingResult bindingResult
    ) {

        // valid 검증에 실패했으면 해당 메시지로 에러 던지기
        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        insertBookService.saveBook(req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("책 추가에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "책 1권 조회", description = "책 1권 조회")
    @GetMapping("/api/books/{id}")
    public ResponseEntity<ApiSuccessResponse<BookEntity>> getBook(
            @PathVariable("id") Long id
    ) {

        BookEntity bookEntity = selectBookService.findById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(bookEntity, "책 조회에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "책 1권 업데이트(커스텀 책만)", description = "책 1권 업데이트(커스텀 책만)")
    @PutMapping("/api/books/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> patchBook(
            @PathVariable("id") Long id,
            @RequestBody ManualBookDto req
    ) {

        updateBookService.updateBook(id, req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("책 업데이트에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "책 1권 삭제(커스텀 책만)", description = "책 1권 삭제(커스텀 책만)")
    @DeleteMapping("/api/books/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteCustomBook(
            @PathVariable("id") Long id) {

        deleteBookService.deleteBook(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("직접 등록 책 삭제에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "엑셀 파일로 책 등록", description = "엑셀 파일 1건을 받아 행 데이터를 추출")
    @PostMapping(value = "/api/books/excel", consumes = "multipart/form-data")
    public ResponseEntity<ApiSuccessResponse<Void>> createBookByExcel(
            @RequestParam("file") MultipartFile file
    ) {

        insertBookService.saveBookByExcel(file);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("엑셀 파일 처리에 성공했습니다.", "SUCCESS"));
    }












    @Validated
    @Operation(summary = "책 일괄 등록", description = "List 타입 책 삽입")
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
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiSuccessResponse.created(upsertResult, "추가한 책이 없습니다.", "SUCCESS"));
        }
        // 삽입된 데이터가 있으면 201 응답
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(upsertResult, "책 추가에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "도서 추천 조회", description = "문학, 비문학 각객 이전에 읽은 책, 현재 수업에 읽어야 하는 책, 다음 수업에 읽어야 하는 책 추천")
    @GetMapping("/api/students/{studentId}/books/recommend")
    public ResponseEntity<ApiSuccessResponse<RecBookRes[][]>> getSliderBooks(
            @PathVariable("studentId") Long studentId
    ) {

        RecBookRes[][] recommended = recommendBookService.recommendBookToRead(studentId);

        if (recommended == null) {
            return ResponseEntity.ok(
                    ApiSuccessResponse.ok(null, "시작 레벨을 설정해주세요.", "FAIL"));
        }

        return ResponseEntity.ok(
                ApiSuccessResponse.ok(recommended, "추천 도서 조회에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "난이도순으로 책 가져오기", description = "난이도, 제목순으로 책 가져오기")
    @GetMapping("/api/books/by-difficulty")
    public ResponseEntity<ApiSuccessResponse<List<BookEntity>>> selectBookByDifficultyAsc() {

        List<BookEntity> bookEntityList = selectBookService.findAllByDifficultyAscAndTitleAsc();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(bookEntityList, "난이도순으로 책 가져오기에 성공했습니다", "SUCCESS"));
    }

//    /**
//     * 통합 도서 검색
//     */
//    @Operation(summary = "통합 도서 검색", description = "제목, 저자, 출판사 키워드로 도서를 검색합니다.")
//    @GetMapping("/api/books/search")
//    public ResponseEntity<ApiSuccessResponse<List<RecommendBookRes>>> searchBooks(@RequestParam("keyword") String keyword) {
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//        List<RecommendBookRes> response = searchBookService.searchBooks(keyword);
//        return ResponseEntity.ok(ApiSuccessResponse.ok(response, "통합 도서 검색에 성공했습니다.", "SUCCESS"));
//    }
}
