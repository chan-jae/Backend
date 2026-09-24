package com.team.student_calendar.security.controller;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.security.service.RefreshTokenService;
import com.team.student_calendar.security.util.JWTUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "리프레시 토큰", description = "RefreshTokenApiController")
public class RefreshTokenApiController {

    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;



    // SameSite=Strict/Lax 였다면 다른 도메인에서 온 요청엔 쿠키를 안붙여서 CSRF 방어가 됬겠지만
    // 리액트, 스프링 서버가 다른 도메인이기 때문에 SameSite=None 로 설정해야해서 별도 로직 필요.
    // preflight 를 유발하지 않는 simple request 에 대해서는 응답을 JS가 읽을 수 있는지만 결정할 뿐
    // 요청이 서버에서 실행되는 것은 막지 못한다.
    // 따라서, 커스텀 헤더로 preflight 를 강제하면 더이상 simple request 로 취급하지 않게 되면
    // OPTIONS 요청을 먼저 보내므로 핵싱 로직 실행을 막을 수 있다. 실제 값은 사용하지 않는다.
    @PostMapping("/api/tokens/reissue")
    public ResponseEntity<ApiSuccessResponse<String>> reissueTokens(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader("X-Refresh-Request") String refreshRequestHeader,
            HttpServletResponse response
    ) {

        String[] tokens = refreshTokenService.reissueTokens(refreshToken);
        String newAccessToken = tokens[0];
        String newRefreshToken = tokens[1];

        ResponseCookie newRefreshCookie = jwtUtil.createCookie("refreshToken", newRefreshToken, "/api/tokens/reissue");
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(newAccessToken, "액세스 토큰 재발급에 성공했습니다.", "SUCCESS"));
    }
}
