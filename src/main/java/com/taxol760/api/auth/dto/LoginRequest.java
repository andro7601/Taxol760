package com.taxol760.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(example = "user@example.com")
        String email,

        @Schema(example = "password123")
        String password
) {
}
