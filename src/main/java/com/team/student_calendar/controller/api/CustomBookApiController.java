package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.CustomBookCreateReq;
import com.team.student_calendar.dto.CustomBookRes;
import com.team.student_calendar.service.book.CustomBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "직접 등록 책", description = "CustomBookApiController")
public class CustomBookApiController {

    private final CustomBookService customBookService;

    @Operation(summary = "직접 등록 책 추가", description = "티칭오션에 없는 책을 제목/난이도/카테고리로 직접 등록")
    @PostMapping("/api/custom-books")
    public ResponseEntity<ApiSuccessResponse<CustomBookRes>> createCustomBook(
            @RequestBody @Valid CustomBookCreateReq req,
            BindingResult bindingResult) {

        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        CustomBookRes res = CustomBookRes.from(customBookService.create(req));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(res, "책 직접 등록에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "직접 등록 책 목록 조회", description = "카테고리·난이도순으로 직접 등록 책 가져오기")
    @GetMapping("/api/custom-books")
    public ResponseEntity<ApiSuccessResponse<List<CustomBookRes>>> getCustomBooks() {

        List<CustomBookRes> list = customBookService.findAll().stream()
                .map(CustomBookRes::from)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(list, "직접 등록 책 목록 조회에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "직접 등록 책 단건 조회", description = "id로 직접 등록 책 가져오기")
    @GetMapping("/api/custom-books/{id}")
    public ResponseEntity<ApiSuccessResponse<CustomBookRes>> getCustomBook(
            @PathVariable("id") Long id) {

        CustomBookRes res = CustomBookRes.from(customBookService.findById(id));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "직접 등록 책 조회에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "직접 등록 책 수정", description = "제목/난이도/카테고리 수정")
    @PutMapping("/api/custom-books/{id}")
    public ResponseEntity<ApiSuccessResponse<CustomBookRes>> updateCustomBook(
            @PathVariable("id") Long id,
            @RequestBody @Valid CustomBookCreateReq req,
            BindingResult bindingResult) {

        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        CustomBookRes res = CustomBookRes.from(customBookService.update(id, req));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "직접 등록 책 수정에 성공했습니다.", "SUCCESS"));
    }

    @Operation(summary = "직접 등록 책 삭제", description = "id로 직접 등록 책 삭제")
    @DeleteMapping("/api/custom-books/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteCustomBook(
            @PathVariable("id") Long id) {

        customBookService.delete(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("직접 등록 책 삭제에 성공했습니다.", "SUCCESS"));
    }
}
