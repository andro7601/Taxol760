package com.taxol760.api.controller.dto;

import com.taxol760.database.models.user.UserRole;

public record CreateUserRequest(
        String email,
        String name,
        String password,
        String phoneNumber,
        UserRole role
) {
}
