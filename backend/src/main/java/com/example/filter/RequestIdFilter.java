package com.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Generate a unique request ID
            String requestId = UUID.randomUUID().toString();

            // Add the request ID to the Mapped Diagnostic Context (MDC)
            MDC.put(MDC_KEY, requestId);

            // Set the request ID as a response header so clients can see it
            response.setHeader(REQUEST_ID_HEADER, requestId);

            // Continue processing the request
            filterChain.doFilter(request, response);
        } finally {
            // Ensure the MDC is cleared after the request is completed
            MDC.remove(MDC_KEY);
        }
    }
}
