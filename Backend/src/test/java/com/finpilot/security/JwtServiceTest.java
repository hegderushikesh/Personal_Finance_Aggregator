package com.finpilot.security;

import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour

        testUser = User.builder()
                .id(42L)
                .name("Rushikesh")
                .email("rushikesh@example.com")
                .authProvider(AuthProvider.GOOGLE)
                .providerId("sub-999")
                .build();
    }

    @Test
    @DisplayName("Generate valid JWT token and extract claims correctly")
    void testGenerateAndExtractClaims() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("rushikesh@example.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("Invalid token validation returns false")
    void testInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtService.isTokenValid(invalidToken));
    }
}
