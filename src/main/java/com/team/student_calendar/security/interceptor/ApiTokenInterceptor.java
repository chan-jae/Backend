//package com.team.student_calendar.security.interceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.authentication.AuthenticationTrustResolver;
//import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.parameters.P;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Slf4j
//@Component
//public class ApiTokenInterceptor implements HandlerInterceptor {
//
//    private final String apiToken;
//    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
//
//    public ApiTokenInterceptor(@Value("${api-token}") String apiToken) {
//        this.apiToken = apiToken;
//    }
//
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            return true;
//        }
//
//        String xApiToken = request.getHeader("X-Api-Token");
//
//        System.out.println("preHandle 시작");
//        // X-Api-Token 헤더 있으면 값 검증
//        if (xApiToken != null) {
//            System.out.println("x api token 헤더 있음");
//            if (!xApiToken.equals(apiToken)) {
//                System.out.println("x api token 값 일치안함 return false");
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return false;
//            }
//            return true;
//        }
//
//        System.out.println("jwt 인증 시작");
//
//        // X-Api-Token 헤더 없으면 JWT 인증 여부 확인
//        /* JWT 발급 됐는데도 isAnonymous가 true로 나오는 이유는 세션(STATE) 방식은 정보를 서버가 다 들고 있어서
//        접속했던 유저가 있으면 SecurityContext안에 Authentication(Principal, Credentials, Authorities) 를 자동으로 채워주는데
//        STATELESS 방식은 자동으로 채워주지 않는다. => JWT 토큰 검증 및 파싱 후에 Authentication의 값을 직접 채워줘야 한다.
//         */
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        boolean isJwtAuthenticated = (
//                authentication != null
//                && authentication.isAuthenticated()
//                && !trustResolver.isAnonymous(authentication)
//        );
//        System.out.println("authentication != null : " + (authentication != null));
//        System.out.println("authentication.isAuthenticated() : " + authentication.isAuthenticated());
//        System.out.println("!trustResolver.isAnonymous(authentication) : " + !trustResolver.isAnonymous(authentication));
//
//        if (!isJwtAuthenticated) {
//            System.out.println("jwt 인증 실패");
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//
//        return true;
//    }
//}
