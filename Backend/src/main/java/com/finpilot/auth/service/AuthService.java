package com.finpilot.auth.service;

import com.finpilot.auth.dto.AuthResponse;
import com.finpilot.auth.dto.LoginRequest;
import com.finpilot.auth.dto.RegisterRequest;
import com.finpilot.auth.dto.SignUpRequest;
import com.finpilot.auth.dto.UserResponse;
import com.finpilot.security.JwtService;
import com.finpilot.user.entity.User;
import com.finpilot.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        SignUpRequest signUpRequest = new SignUpRequest(request.getName(), request.getEmail(), request.getPassword());
        User user = userService.registerLocalUser(signUpRequest);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userService.authenticateLocalUser(request);
        log.info("Login successful for user: {}", user.getEmail());
        return toAuthResponse(user);
    }

    public AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .user(UserResponse.from(user))
                .build();
    }
}
