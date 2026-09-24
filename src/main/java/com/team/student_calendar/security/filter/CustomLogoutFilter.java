package com.team.student_calendar.security.filter;

import com.team.student_calendar.security.service.RefreshTokenService;
import com.team.student_calendar.security.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class CustomLogoutFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (!"/api/r-token/logout".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        if (!"POST".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }


        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("쿠키없음");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Claims claims = jwtUtil.getClaims(refreshToken);
        String username = claims.getSubject();
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshTokenService.deleteRefreshToken(refreshToken);

        ResponseCookie refreshCookie = jwtUtil.deleteRefreshTokenCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);

        log.info("logout success [{}]", username);
    }
}
