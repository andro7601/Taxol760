package com.taxol760.api.auth.dto.requests;

public record LoginRequest(
        String email,
        String password
) {
}
