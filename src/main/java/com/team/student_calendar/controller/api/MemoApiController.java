package com.team.student_calendar.controller.api;

import com.team.student_calendar.dto.MemoCreateReq;
import com.team.student_calendar.dto.MemoCreateRes;
import com.team.student_calendar.service.memo.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students/{studentId}/memos")
public class MemoApiController {

    private final MemoService memoService;

    @PostMapping
    public ResponseEntity<MemoCreateRes> createMemo(
            @PathVariable("studentId") Long studentId,
            @RequestBody MemoCreateReq req) {

        MemoCreateRes result = memoService.createMemo(studentId, req);

        // 200
        return ResponseEntity.ok(result);
    }
}