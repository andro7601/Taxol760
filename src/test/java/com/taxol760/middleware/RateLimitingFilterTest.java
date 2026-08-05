package com.taxol760.middleware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private RateLimitingFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Limit is 15 requests per 10 seconds (matches SecurityConfig)
        filter = new RateLimitingFilter(redisTemplate, 15, Duration.ofSeconds(10));

        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/users/me");
        request.setRemoteAddr("192.168.1.1");

        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void filter_returns429_whenRequestCountExceedsLimit() throws Exception {
        // Redis reports this IP has made 16 requests — over the limit of 15
        when(valueOperations.increment(anyString())).thenReturn(16L);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        // Request must be blocked before reaching the controller
        assertEquals(null, filterChain.getRequest());
    }

    @Test
    void filter_passes_whenRequestCountIsWithinLimit() throws Exception {
        // Redis reports only 5 requests so far — well within limit
        when(valueOperations.increment(anyString())).thenReturn(5L);

        filter.doFilterInternal(request, response, filterChain);

        // Request went through normally (MockFilterChain default is 200)
        assertEquals(200, response.getStatus());
    }

    @Test
    void filter_returns429_exactlyAtLimitPlusOne() throws Exception {
        // Edge case: the 16th request (limit + 1) must be blocked
        when(valueOperations.increment(anyString())).thenReturn(16L);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
    }
}
