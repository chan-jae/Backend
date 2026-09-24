package com.team.student_calendar.security.filter;

import com.team.student_calendar.common.enums.UserRole;
import com.team.student_calendar.security.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final String apiToken;
    private final List<RequestMatcher> permitAllMatchers;

    public JWTFilter(JWTUtil jwtUtil, String apiToken, String[] permitAllPaths) {
        this.jwtUtil = jwtUtil;
        this.apiToken = apiToken;
        this.permitAllMatchers = Arrays.stream(permitAllPaths)
                .<RequestMatcher>map(path -> PathPatternRequestMatcher.withDefaults().matcher(path))
                .toList();
    }


    // SecurityConfig의 permitAll 경로는 인증 헤더 검사 없이 통과시킴 (두 곳에서 따로 관리하면 어긋나기 쉬워서 경로 목록을 생성자로 주입받음)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return permitAllMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String xApiToken = request.getHeader("X-Api-Token");

        // 로그인을 거친 게 아니라 토큰 값만 검증한 것이므로, @AuthenticationPrincipal로 실제 유저 정보를 꺼내 쓰는 API는 이 경로로 호출하면 안 됨.
        if (xApiToken != null) {
            if (!xApiToken.equals(apiToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=utf-8");
                return;
            }

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name()));
            Authentication auth = new UsernamePasswordAuthenticationToken("api-client", null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            return;
        }

        String accessToken = authorization.substring(7);

        try {
            Claims claims = jwtUtil.getClaims(accessToken);

            String username = claims.getSubject();
            String role = claims.get("role", String.class); // ROLE_USER 같은 형태
            String tokenTYpe = claims.get("tokenType", String.class);


            if (!"ACCESS".equals(tokenTYpe)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=utf-8");
                return;
            }

            // 유저가 권한을 ADMIN, USER 중 1개만 가지도록 했으니까 singletonList(불변, 추가/수정 불가능) 사용.
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) { // 만료된 토큰 catch
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"errorCode\":\"JWT-EXPIRED\"}");
        } catch (Exception e) { // 위조된 키로 서명이 되어 있는 등 최종 에러 catch
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
        }
    }
}
