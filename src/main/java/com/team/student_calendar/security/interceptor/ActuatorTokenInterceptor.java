package com.team.student_calendar.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ActuatorTokenInterceptor implements HandlerInterceptor {

    private final String actuatorToken;

    public ActuatorTokenInterceptor(@Value("${actuator-token}") String actuatorToken) {
        this.actuatorToken = actuatorToken;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String xActuatorToken = request.getHeader("X-Actuator-Token");

        if (xActuatorToken == null || !xActuatorToken.equals(actuatorToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("invalid actuator token");
            return false;
        }
        return true;
    }
}
