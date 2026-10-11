package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.enums.IsbnLookupStatus;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.IsbnBookRes;
import com.team.student_calendar.dto.QuestionSheetRes;
import com.team.student_calendar.service.isbnlookup.InsertIsbnLookupService;
import com.team.student_calendar.service.isbnlookup.SelectIsbnLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ISBN 바코드 저장 로그", description = "IsbnLookupApiController")
public class IsbnLookupApiController {

    private final InsertIsbnLookupService insertIsbnLookupService;
    private final SelectIsbnLookupService selectIsbnLookupService;


    @Operation(summary = "ISBN PENDING 등록", description = "외부 API 조회 전 ISBN을 PENDING 상태로 등록하고 조회 기록 id 반환")
    @PostMapping("/api/books/isbn/{isbn}/pending")
    public ResponseEntity<ApiSuccessResponse<Long>> postIsbnPending(
            @PathVariable("isbn") String isbn
    ) {

        validateIsbn(isbn);

        Long lookupId = insertIsbnLookupService.saveIsbnPending(isbn);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(lookupId, "ISBN 바코드 PENDING 등록에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "ISBN 조회 기록으로 책 저장", description = "PENDING 등록 때 받은 id로 YES24 + 정보나루 API 병렬 호출해서 책 저장")
    @PostMapping("/api/books/isbn/lookups/{id}")
    public ResponseEntity<ApiSuccessResponse<IsbnBookRes>> postBookByIsbn(
            @PathVariable("id") Long id
    ) {

        IsbnBookRes res = insertIsbnLookupService.saveBookByIsbn(id);

        if (res.status() == IsbnLookupStatus.FAILED) {
            return ResponseEntity.ok(
                    ApiSuccessResponse.ok(res, "ISBN 바코드로 책 정보를 찾지 못했습니다.", "FAILED"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(res, "ISBN 바코드로 책 저장에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "ISBN 조회 기록 전체 조회", description = "ISBN 바코드 전체 이력 조회")
    @GetMapping("/api/books/isbn/lookups")
    public ResponseEntity<ApiSuccessResponse<List<IsbnBookRes>>> getIsbnLookups() {

        List<IsbnBookRes> res = selectIsbnLookupService.findAll();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "ISBN 바코드 전체 이력 조회에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "책 활동지 문제 생성 요청", description = "Claude API로 활동지 문제 생성을 백그라운드로 시작하고 바로 응답 (결과는 GET으로 폴링)")
    @PostMapping("/api/books/{bookId}/question-sheet")
    public ResponseEntity<ApiSuccessResponse<Void>> postQuestionSheet(
            @PathVariable("bookId") Long bookId
    ) {

        insertIsbnLookupService.requestQuestionSheet(bookId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiSuccessResponse.ok("책 활동지 문제 생성 요청에 성공했습니다.", "C_PENDING"));
    }


    @Operation(summary = "생성 완료된 책 활동지 문제 조회", description = "생성이 끝난(SUCCESS/FAILED) 활동지 문제를 lookupId, status 목록으로 응답 (폴링용, 한 번 응답한 결과는 다시 오지 않음)")
    @GetMapping("/api/books/question-sheets")
    public ResponseEntity<ApiSuccessResponse<List<QuestionSheetRes>>> getQuestionSheets() {

        List<QuestionSheetRes> res = insertIsbnLookupService.findDoneQuestionSheets();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "생성 완료된 책 활동지 문제 조회에 성공했습니다.", "SUCCESS"));
    }








    private void validateIsbn(String isbn) {

        if (!isbn.matches("\\d{13}")) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR, "ISBN은 13자리 숫자여야 합니다.");
        }
    }
}
