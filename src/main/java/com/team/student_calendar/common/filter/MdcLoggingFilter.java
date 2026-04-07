package com.team.student_calendar.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

public class MdcLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String traceId = UUID.randomUUID().toString().substring(0, 8);

        MDC.put("traceId", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
