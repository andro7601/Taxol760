package com.taxol760.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {
    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final String REDIS_KEY_PREFIX = "rate-limit:";
    private static final String UNKNOWN_CLIENT = "unknown";

    private final StringRedisTemplate redisTemplate;
    private final int maxRequests;
    private final Duration window;

    public RateLimitingFilter(StringRedisTemplate redisTemplate, int maxRequests, Duration window) {
        this.redisTemplate = redisTemplate;
        this.maxRequests = maxRequests;
        this.window = window;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long requestCount = incrementRequestCount(request);
        if (requestCount > maxRequests) {
            response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
            writeError(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private long incrementRequestCount(HttpServletRequest request) {
        String redisKey = buildRedisKey(request);
        Long requestCount = redisTemplate.opsForValue().increment(redisKey);

        if (requestCount != null && requestCount == 1) {
            redisTemplate.expire(redisKey, window);
        }

        return requestCount == null ? 0 : requestCount;
    }

    private String buildRedisKey(HttpServletRequest request) {
        return REDIS_KEY_PREFIX
                + resolveClientIp(request)
                + ":"
                + request.getMethod()
                + ":"
                + request.getRequestURI();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String remoteAddress = request.getRemoteAddr();
        return remoteAddress == null || remoteAddress.isBlank() ? UNKNOWN_CLIENT : remoteAddress;
    }

    private void writeError(HttpServletResponse response) throws IOException {
        response.setStatus(TOO_MANY_REQUESTS_STATUS);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Too many requests\"}");
    }
}
