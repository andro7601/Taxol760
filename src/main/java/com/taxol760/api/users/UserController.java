package com.taxol760.api.users;

import com.taxol760.api.users.dto.UserResponse;
import com.taxol760.service.auth.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }
}
