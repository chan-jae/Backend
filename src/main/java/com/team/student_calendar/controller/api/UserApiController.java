package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.RefreshRequestDTO;
import com.team.student_calendar.dto.UserRequestDTO;
import com.team.student_calendar.security.service.RefreshTokenService;
import com.team.student_calendar.security.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "유저", description = "UserApiController")
public class UserApiController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;



    @PostMapping("/api/users")
    public ResponseEntity<ApiSuccessResponse<Void>> join(
            @RequestBody UserRequestDTO dto
            ) {

        userService.join(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("회원가입에 성공했습니다.", "SUCCESS"));
    }


    @PostMapping("/api/access-token/reissue")
    public ResponseEntity<ApiSuccessResponse<String>> reissueRefreshToken(
            @RequestBody RefreshRequestDTO dto
            ) {

        String newRefreshToken = refreshTokenService.reissueAccessToken(dto.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(newRefreshToken, "액세스 토큰 재발급에 성공했습니다.", "SUCCESS"));
    }
}
