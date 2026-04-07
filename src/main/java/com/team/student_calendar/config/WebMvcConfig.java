package com.team.student_calendar.config;

import com.team.student_calendar.common.filter.ApiLoggingFilter;
import com.team.student_calendar.common.filter.MdcLoggingFilter;
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
    public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilter() {
        FilterRegistrationBean<MdcLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MdcLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("mdcLoggingFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilter() {
        FilterRegistrationBean<ApiLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addUrlPatterns("/api/*");
        registration.setName("apiLoggingFilter");
        return registration;
    }
}
