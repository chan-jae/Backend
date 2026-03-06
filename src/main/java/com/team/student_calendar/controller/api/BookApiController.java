package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.dto.BookCreateReq;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.service.book.InsertBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "책", description = "BookApiController")

public class BookApiController {

    private final InsertBookService insertBookService;


//    /**
//     * 책 1개만 생성
//     * @param bookCreateReq 책 정보
//     * */
//    @PostMapping("/api/books")
//    public ApiSuccessResponse<Void> createBook(
//            @RequestBody BookCreateReq bookCreateReq
//    ) {
//
//
//
//
//    }


    /**
     * 책 여러개 생성
     * @param bookCreateReqList 책 정보 List
     * */
    @Validated
    @Operation(summary = "여러책 삽입", description = "List 타입 책 삽입")
    @PostMapping("/api/books")
    public ResponseEntity<ApiSuccessResponse<Void>> createBook(
            @RequestBody List<@Valid BookCreateReq> bookCreateReqList,
            BindingResult bindingResult
            ) {

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
}
