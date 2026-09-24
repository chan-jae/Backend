package com.team.student_calendar.security.controller;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.security.service.RegisterTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "가입 토큰", description = "RegisterTokenApiController")
public class RegisterTokenApiController {

    private final RegisterTokenService registerTokenService;


    // ADMIN 전용, 가입 토큰 발급 (기존에 발급된 토큰이 있다면 삭제 후 새로 발급, 항상 1개만 존재)
    @PostMapping("/api/admin/register-tokens")
    public ResponseEntity<ApiSuccessResponse<String>> issueRegisterToken() {

        String token = registerTokenService.issueToken();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(token, "가입 토큰 발급에 성공했습니다.", "SUCCESS"));
    }
}
