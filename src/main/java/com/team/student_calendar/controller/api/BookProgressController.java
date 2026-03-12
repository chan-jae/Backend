package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.BookProgressUpdateReq;
import com.team.student_calendar.dto.BookProgressUpdateRes;
import com.team.student_calendar.service.book.progress.UpdateBookProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "책 진행상황", description = "BookProgressController")
public class BookProgressController {

    private final UpdateBookProgressService updateBookProgressService;



    @Operation(summary = "책 다음으로 업데이트", description = "다음 난이도의 책으로 업데이트")
    @PatchMapping("/api/book-progresses/next")
    public ResponseEntity<ApiSuccessResponse<BookProgressUpdateRes>> nextBookProgress(
            @RequestBody BookProgressUpdateReq req
            ) {

        BookProgressUpdateRes nextBook = updateBookProgressService.nextBookProgress(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(nextBook, "다음 난이도의 책으로 업데이트에 성공했습니다.", "SUCCESS"));
    }



    @Operation(summary = "책 이전으로 업데이트", description = "이전 난이도의 책으로 업데이트")
    @PatchMapping("/api/book-progresses/prev")
    public ResponseEntity<ApiSuccessResponse<BookProgressUpdateRes>> prevBookProgress(
            @RequestBody BookProgressUpdateReq req
    ) {

        BookProgressUpdateRes nextBook = updateBookProgressService.prevBookProgress(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(nextBook, "이전 난이도의 책으로 업데이트에 성공했습니다.", "SUCCESS"));
    }
}
