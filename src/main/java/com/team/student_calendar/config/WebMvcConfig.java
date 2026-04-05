package com.team.student_calendar.config;

import com.team.student_calendar.common.filter.ApiLoggingFilter;
import com.team.student_calendar.security.interceptor.ApiTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiTokenInterceptor apiTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiTokenInterceptor)
                .addPathPatterns("/api/**");
    }

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> logFilter() {
        FilterRegistrationBean<ApiLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/api/*");
        registration.setName("apiLoggingFilter");
        return registration;
    }

}
