package com.team.student_calendar.security.config;

import com.team.student_calendar.security.filter.JWTFilter;
import com.team.student_calendar.security.filter.LoginFilter;
import com.team.student_calendar.security.handler.LoginSuccessHandler;
import com.team.student_calendar.security.util.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final LoginSuccessHandler loginSuccessHandler;
    private final JWTUtil jwtUtil;
    private final String apiToken;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            LoginSuccessHandler loginSuccessHandler,
            JWTUtil jwtUtil,
            @Value("${api-token}") String apiToken
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.jwtUtil = jwtUtil;
        this.apiToken = apiToken;
    }



    // 원래는 자동으로 생성해주지만 Filter단에서 써야하기 때문에 미리 빈으로 등록.
    // AuthenticationConfiguration는 스프링이 미리 등록해줘서 생성자 주입 가능
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 기본 bcrypt 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 필터 비활성화
        /* JWT는 Authorization: Bearer <token>를 주로 헤더에 실어서 보내서 브라우저가 자동으로
        커스텀 헤더를 설정 안하기 때문에 비활성화 해도 무관.
        단, 토큰을 Cookie에 저장하면 브라우저가 자동으로 세팅하기 때문에 위험함.
         */
        http
                .csrf(csrf -> csrf.disable());

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 기본 form 로그인 비활성화
        /* multipart/form-data 를 받는 기본 로그인 필터 비활성화 하고
        json 타입을 받아서 처리하는 필터를 새로 만들어야 함. => LoginFilter
        기본 로그인 설정시 사용되는 필터인 UsernamePasswordAuthenticationFilter와 아키텍처는 거의 똑같고
        multipart/form-data 대신 JSON을 받는 부분만 수정할꺼여서 일부만 수정하면 됨.
         */
        http
                .formLogin(login -> login.disable());

        // 경로별 인가
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users").permitAll()
                        .requestMatchers("/api/tokens/reissue").permitAll()

                        .requestMatchers("/api/v1/user").hasRole("USER")
                        .anyRequest().hasRole("ADMIN")
                );

        // 커스텀 필터 추가
        http
                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        // SecurityContextHolderFilter.class 를 CorsFilter.class 로 수정
        // JWTFilter가 CorsFilter 보다 먼저 실행되는데 직접 응답설정 할 때
        // CorsFilter 통과안해서 직접 설정한 401 에러가 아닌 CORS 관련 에러가 발생함
        http
                .addFilterAfter(new JWTFilter(jwtUtil, apiToken), CorsFilter.class);

        // 세션 설정 STATELESS
        /* 기존 세션 방식은 로그인을 하면 해당 세션 정보를 서버에서 계속 들고있는 것과 다르게
        JWT는 요청~응답이 끝나면 세션을 지우는 방식. 즉, 세션 정보를 서버가 계속 저장하고
        있지 않기 때문에 STATELESS 설정.
        */
        http
                .sessionManagement(sessiong -> sessiong
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        return http.build();
    }
}
