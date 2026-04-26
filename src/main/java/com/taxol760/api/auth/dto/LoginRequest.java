package com.taxol760.api.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
