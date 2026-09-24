package com.team.student_calendar.security.handler;

import com.team.student_calendar.common.exception.domain.UserErrorCode;
import com.team.student_calendar.common.response.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();


    // 아이디가 없거나 비밀번호가 틀린 경우 모두 여기로 모여서 같은 메시지로 응답함 (계정 존재 여부 노출 방지)
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        ApiErrorResponse errorResponse = ApiErrorResponse.error(UserErrorCode.INVALID_LOGIN_INFO);

        response.setStatus(UserErrorCode.INVALID_LOGIN_INFO.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
