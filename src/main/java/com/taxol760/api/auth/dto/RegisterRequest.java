package com.taxol760.api.auth.dto;

import com.taxol760.databaseANDcache.model.user.UserRole;

public record RegisterRequest(
        String email,
        String name,
        String password,
        String phoneNumber,
        UserRole role
) {
}
