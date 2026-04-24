package com.taxol760.api.dto;

import com.taxol760.database.models.user.UserModel;
import com.taxol760.database.models.user.UserRole;

public record UserResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        UserRole role
) {
    public static UserResponse from(UserModel user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
}
