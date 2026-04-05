package com.team.student_calendar.common.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final int REQUEST_CACHE_LIMIT = 65_536;  // 64KB
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String method = request.getMethod();
        String endpoint = buildEndpoint(request);
        log.info("[{} {}] request accepted", method, endpoint);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);

            log.debug("request body: {}", new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));

        } finally {
            long elapsedMs = System.currentTimeMillis() - start;

            String message = extractJsonMessageField(responseWrapper);
            log.info(
                    "[{} {} ({}ms)] response : status={}, message={}",
                    method,
                    endpoint,
                    elapsedMs,
                    responseWrapper.getStatus(),
                    message != null ? message : "");

            log.debug("response body: {}", new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));

            // 캐싱된 응답을 실제로 응답하기
            responseWrapper.copyBodyToResponse();
        }
    }

    private static String extractJsonMessageField(ContentCachingResponseWrapper response) {

        byte[] body = response.getContentAsByteArray();
        if (body.length == 0) {
            return null;
        }
        try {
            String responseBody = new String(body, StandardCharsets.UTF_8);
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            String message = root.path("message").asText();
            return message.isEmpty() ? null : message;
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildEndpoint(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null || query.isEmpty() ? uri : uri + "?" + query;
    }
}
