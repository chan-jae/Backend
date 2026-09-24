package com.team.student_calendar.security.controller;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.UserRequestDTO;
import com.team.student_calendar.dto.UserInfoDTO;
import com.team.student_calendar.security.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "유저", description = "UserApiController")
public class UserApiController {

    private final UserService userService;


    @Validated
    @PostMapping("/api/users")
    public ResponseEntity<ApiSuccessResponse<Void>> join(
            @RequestBody @Valid UserRequestDTO dto,
            BindingResult bindingResult
            ) {

        if (bindingResult.hasFieldErrors()) {
            throw new BaseException(CommonErrorCode.PARAMETER_ERROR,
                    bindingResult.getFieldError().getDefaultMessage());
        }

        userService.join(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("회원가입에 성공했습니다.", "SUCCESS"));
    }


    @GetMapping("/api/users/me")
    public ResponseEntity<ApiSuccessResponse<UserInfoDTO>> getMyInfo(
            @AuthenticationPrincipal String username
    ) {

        UserInfoDTO dto = userService.getMyInfo(username);

        return ResponseEntity.ok(ApiSuccessResponse.ok(dto, "내 정보 조회에 성공했습니다.", "SUCCESS"));
    }
}
