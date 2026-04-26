package com.taxol760.api.users.dto;

import com.taxol760.database.model.user.UserModel;
import com.taxol760.database.model.user.UserRole;

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
