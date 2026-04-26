package com.taxol760.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class IdempotencyKeyFilter extends OncePerRequestFilter {
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String REDIS_KEY_PREFIX = "idempotency:";
    private static final String PROTECTED_METHOD = "POST";
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/rides",
            "/api/rides/*/accept",
            "/api/rides/*/start"
    );

    private final StringRedisTemplate redisTemplate;
    private final Duration keyTtl;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public IdempotencyKeyFilter(StringRedisTemplate redisTemplate, Duration keyTtl) {
        this.redisTemplate = redisTemplate;
        this.keyTtl = keyTtl;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!shouldProtect(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing Idempotency-Key header");
            return;
        }

        if (!reserveIdempotencyKey(idempotencyKey)) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "Request was already submitted");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldProtect(HttpServletRequest request) {
        if (!PROTECTED_METHOD.equals(request.getMethod())) {
            return false;
        }

        String requestPath = request.getRequestURI();
        return PROTECTED_PATHS.stream().anyMatch(path -> pathMatcher.match(path, requestPath));
    }

    private boolean reserveIdempotencyKey(String idempotencyKey) {
        String redisKey = REDIS_KEY_PREFIX + idempotencyKey;
        Boolean wasReserved = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", keyTtl);
        return Boolean.TRUE.equals(wasReserved);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
