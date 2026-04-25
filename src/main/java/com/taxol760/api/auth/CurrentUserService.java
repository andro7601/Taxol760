package com.taxol760.api.auth;

import com.taxol760.database.models.user.UserModel;
import com.taxol760.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserService userService;

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String username && !"anonymousUser".equals(username)) {
            return username;
        }

        throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
    }

    @Transactional(readOnly = true)
    public UserModel getCurrentUser() {
        return userService.getUserByEmail(getCurrentUserEmail());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
