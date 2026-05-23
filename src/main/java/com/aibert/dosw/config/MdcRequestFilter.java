package com.aibert.dosw.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class MdcRequestFilter extends OncePerRequestFilter {

    static final String MDC_CORRELATION_ID = "correlationId";
    static final String MDC_STUDENT_ID     = "studentId";
    static final String MDC_HTTP_METHOD    = "httpMethod";
    static final String MDC_HTTP_PATH      = "httpPath";

    static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    static final String HEADER_USER_ID        = "X-User-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String correlationId = resolveCorrelationId(request);
        String studentId     = resolveStudentId(request);

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_STUDENT_ID,     studentId);
        MDC.put(MDC_HTTP_METHOD,    request.getMethod());
        MDC.put(MDC_HTTP_PATH,      request.getRequestURI());

        response.setHeader(HEADER_CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_STUDENT_ID);
            MDC.remove(MDC_HTTP_METHOD);
            MDC.remove(MDC_HTTP_PATH);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER_CORRELATION_ID);
        if (incoming != null && !incoming.isBlank()) {
            return incoming;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String resolveStudentId(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        return (userId != null && !userId.isBlank()) ? userId : "anonymous";
    }
}
