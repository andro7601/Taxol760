package com.taxol760.api.users.dto;

import com.taxol760.database.model.user.UserRole;

public record CreateUserRequest(
        String email,
        String name,
        String password,
        String phoneNumber,
        UserRole role
) {
}
