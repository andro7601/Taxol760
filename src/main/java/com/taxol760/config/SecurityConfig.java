package com.taxol760.config;

import com.taxol760.middleware.JwtAuthenticationFilter;
import com.taxol760.middleware.IdempotencyKeyFilter;
import com.taxol760.middleware.RateLimitingFilter;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            IdempotencyKeyFilter idempotencyKeyFilter,
            RateLimitingFilter rateLimitingFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(idempotencyKeyFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, IdempotencyKeyFilter.class)
                .build();
    }

    @Bean
    public IdempotencyKeyFilter idempotencyKeyFilter(StringRedisTemplate redisTemplate) {
        return new IdempotencyKeyFilter(redisTemplate, Duration.ofMinutes(10));
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter(StringRedisTemplate redisTemplate) {
        return new RateLimitingFilter(redisTemplate, 60, Duration.ofMinutes(1));
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<IdempotencyKeyFilter> idempotencyKeyFilterRegistration(
            IdempotencyKeyFilter idempotencyKeyFilter
    ) {
        FilterRegistrationBean<IdempotencyKeyFilter> registration = new FilterRegistrationBean<>(idempotencyKeyFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(
            RateLimitingFilter rateLimitingFilter
    ) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>(rateLimitingFilter);
        registration.setEnabled(false);
        return registration;
    }
}
