package com.finpilot.auth.controller;

import com.finpilot.auth.dto.AuthResponse;
import com.finpilot.auth.dto.LoginRequest;
import com.finpilot.auth.dto.RegisterRequest;
import com.finpilot.auth.dto.UserResponse;
import com.finpilot.auth.service.AuthService;
import com.finpilot.security.UserPrincipal;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserPrincipal testUserPrincipal;
    private User testUser;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(100L)
                .name("Rushikesh Hegde")
                .email("rushikesh@example.com")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .providerId(null)
                .profileImageUrl(null)
                .build();

        testUserPrincipal = UserPrincipal.create(testUser);
        authResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .user(UserResponse.from(testUser))
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register returns 200 OK and JWT auth response")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("Rushikesh Hegde", "rushikesh@example.com", "password123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        assertEquals("rushikesh@example.com", response.getBody().getUser().getEmail());
        assertEquals("USER", response.getBody().getUser().getRole());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK and JWT auth response")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("rushikesh@example.com", "password123");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        assertEquals(100L, response.getBody().getUser().getId());
    }

    @Test
    @DisplayName("GET /api/auth/me returns 200 OK and user details when authenticated")
    void testGetCurrentUser_Authenticated() {
        ResponseEntity<?> response = authController.getCurrentUser(testUserPrincipal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/auth/me returns 401 Unauthorized when unauthenticated")
    void testGetCurrentUser_Unauthenticated() {
        ResponseEntity<?> response = authController.getCurrentUser(null);

        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/test/protected returns 200 OK when authenticated")
    void testGetProtectedTestEndpoint_Authenticated() {
        ResponseEntity<?> response = authController.getProtectedTestEndpoint(testUserPrincipal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/test/protected returns 401 Unauthorized when unauthenticated")
    void testGetProtectedTestEndpoint_Unauthenticated() {
        ResponseEntity<?> response = authController.getProtectedTestEndpoint(null);

        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
