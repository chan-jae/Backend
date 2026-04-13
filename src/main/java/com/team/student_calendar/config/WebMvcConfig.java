package com.team.student_calendar.config;

import com.team.student_calendar.common.filter.ApiLoggingFilter;
import com.team.student_calendar.common.filter.MdcLoggingFilter;
import com.team.student_calendar.security.interceptor.ActuatorTokenInterceptor;
import com.team.student_calendar.security.interceptor.ApiTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiTokenInterceptor apiTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiTokenInterceptor)
                .addPathPatterns("/api/**");
        // Actuator는 WebMvcEndpointHandlerMapping 등 별도 매핑을 쓰므로
        // addInterceptors만으로는 적용되지 않는 경우가 많음 → MappedInterceptor 빈 필요
    }

    @Bean
    public MappedInterceptor actuatorTokenMappedInterceptor(ActuatorTokenInterceptor actuatorTokenInterceptor) {
        return new MappedInterceptor(new String[] { "/actuator/**" }, actuatorTokenInterceptor);
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
