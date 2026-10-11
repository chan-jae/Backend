package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.ApiUsageRes;
import com.team.student_calendar.service.apiusage.SelectApiUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "외부 API 사용량", description = "ApiUsageApiController")
public class ApiUsageApiController {

    private final SelectApiUsageService selectApiUsageService;


    @Operation(summary = "외부 API 사용량 조회", description = "YES24, 정보나루 API의 오늘 사용량과 남은 횟수 (매일 0시 기준 초기화)")
    @GetMapping("/api/api-usages")
    public ResponseEntity<ApiSuccessResponse<List<ApiUsageRes>>> getApiUsages() {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(selectApiUsageService.findApiUsages(), "외부 API 사용량 조회에 성공했습니다.", "SUCCESS"));
    }
}
