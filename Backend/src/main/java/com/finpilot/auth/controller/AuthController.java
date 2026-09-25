package com.finpilot.auth.controller;

import com.finpilot.auth.dto.AuthResponse;
import com.finpilot.auth.dto.LoginRequest;
import com.finpilot.auth.dto.MessageResponse;
import com.finpilot.auth.dto.RegisterRequest;
import com.finpilot.auth.dto.UserResponse;
import com.finpilot.auth.service.AuthService;
import com.finpilot.security.UserPrincipal;
import com.finpilot.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping({"/api/auth/register", "/api/auth/signup"})
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(new MessageResponse("Logged out successfully."));
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        User user = currentUser.getUser();
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/api/test/protected")
    public ResponseEntity<MessageResponse> getProtectedTestEndpoint(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new MessageResponse("You have access to a protected endpoint"));
    }
}
