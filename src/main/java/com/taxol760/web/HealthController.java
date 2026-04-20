package com.taxol760.web;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    private final StringRedisTemplate redisTemplate;

    public HealthController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        String redisStatus = Boolean.TRUE.equals(redisTemplate.hasKey("health")) ? "connected" : "reachable";

        return Map.of(
                "app", "Taxol760",
                "status", "ok",
                "redis", redisStatus);
    }
}
