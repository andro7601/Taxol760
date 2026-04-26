package com.taxol760.service.auth;

import com.taxol760.api.auth.dto.LoginRequest;
import com.taxol760.api.auth.dto.RegisterRequest;
import com.taxol760.api.auth.dto.TokenResponse;
import com.taxol760.database.model.user.UserModel;
import com.taxol760.database.model.user.UserRole;
import com.taxol760.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        UserModel user = findUserByEmailOrThrowBadCredentials(request.email());
        ensurePasswordMatches(request.password(), user);
        return createTokenResponse(user);
    }

    public TokenResponse register(RegisterRequest request) {
        ensureEmailIsNotDuplicate(request.email());
        UserModel user = createUserFromRegisterRequest(request);
        return createTokenResponse(user);
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    public void ensureEmailIsNotDuplicate(String email) {
        if (isEmailDuplicate(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    public UserModel findUserByEmailOrThrowBadCredentials(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    public void ensurePasswordMatches(String rawPassword, UserModel user) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public UserModel createUserFromRegisterRequest(RegisterRequest request) {
        UserModel user = new UserModel();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(resolveRole(request.role()));

        return userRepository.save(user);
    }

    public TokenResponse createTokenResponse(UserModel user) {
        return new TokenResponse(jwtService.generateToken(user));
    }

    public UserRole resolveRole(UserRole role) {
        return role == null ? UserRole.USER : role;
    }
}
