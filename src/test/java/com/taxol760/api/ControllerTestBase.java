package com.taxol760.api;

import com.taxol760.middleware.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;

public abstract class ControllerTestBase {
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
}
