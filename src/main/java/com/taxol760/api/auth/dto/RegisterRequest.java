package com.taxol760.api.auth.dto;

import com.taxol760.databaseANDcache.model.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
        String email,

        String name,

        String password,

        String phoneNumber,

        UserRole role
) {
}
