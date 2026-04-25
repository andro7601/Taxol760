package com.taxol760.api.auth.dto.requests;

import com.taxol760.database.models.user.UserRole;

public record RegisterRequest(
        String email,
        String name,
        String password,
        String phoneNumber,
        UserRole role
) {
}
