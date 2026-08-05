package com.taxol760.middleware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyKeyFilterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private IdempotencyKeyFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        filter = new IdempotencyKeyFilter(redisTemplate, Duration.ofHours(24));

        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/rides");

        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void filter_returns409_whenIdempotencyKeyAlreadyUsed() throws Exception {
        // Redis returns false meaning the key already exists — duplicate request
        request.addHeader("Idempotency-Key", "ride-uuid-abc123");
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(409, response.getStatus());
        // The request must NEVER reach the controller
        assertEquals(null, filterChain.getRequest());
    }

    @Test
    void filter_returns400_whenIdempotencyKeyHeaderIsMissing() throws Exception {
        // No Idempotency-Key header at all on a protected POST endpoint
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(valueOperations);
    }

    @Test
    void filter_passes_whenIdempotencyKeyIsNew() throws Exception {
        // First time this key is seen — Redis reserves it and the request goes through
        request.addHeader("Idempotency-Key", "ride-uuid-fresh");
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        // FilterChain advanced means the request went through
        assertEquals(200, response.getStatus());
    }
}
