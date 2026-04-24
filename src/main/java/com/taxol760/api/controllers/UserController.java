package com.taxol760.api.controllers;

import com.taxol760.api.dto.CreateUserRequest;
import com.taxol760.api.dto.UserResponse;
import com.taxol760.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(
                request.email(),
                request.name(),
                request.password(),
                request.phoneNumber(),
                request.role()
        ));
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
    }
}
