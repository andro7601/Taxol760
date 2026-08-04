package com.taxol760.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
        @Schema(example = "user@example.com")
        String email,

        @Schema(example = "John Doe")
        String name,

        @Schema(example = "password123")
        String password,

        @Schema(example = "5551234567")
        String phoneNumber
) {}
