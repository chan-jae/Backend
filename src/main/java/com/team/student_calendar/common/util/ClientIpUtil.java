package com.team.student_calendar.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpUtil {

    // 프록시(nginx)가 넣어주는 X-Real-IP 우선, 없으면 remoteAddr
    public static String getClientIp(HttpServletRequest request) {

        String clientIp = request.getHeader("X-Real-IP");
        System.out.println("X-Real-IP : " + clientIp);
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
