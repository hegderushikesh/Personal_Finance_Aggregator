package com.finpilot.user.service;

import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import com.finpilot.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private UserService userService;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put("sub", "google-sub-12345");
        attributes.put("email", "testuser@example.com");
        attributes.put("name", "Test User");
        attributes.put("picture", "https://example.com/photo.jpg");
    }

    @Test
    @DisplayName("Register new local user successfully")
    void testRegisterLocalUser_Success() {
        com.finpilot.auth.dto.SignUpRequest request = new com.finpilot.auth.dto.SignUpRequest("Rushikesh", "rushi@example.com", "secret123");
        when(userRepository.findByEmail("rushi@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword123");

        User savedUser = User.builder()
                .id(10L)
                .name("Rushikesh")
                .email("rushi@example.com")
                .password("encodedPassword123")
                .authProvider(AuthProvider.LOCAL)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerLocalUser(request);

        assertNotNull(result);
        assertEquals("rushi@example.com", result.getEmail());
        assertEquals(AuthProvider.LOCAL, result.getAuthProvider());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Authenticate local user successfully")
    void testAuthenticateLocalUser_Success() {
        com.finpilot.auth.dto.LoginRequest request = new com.finpilot.auth.dto.LoginRequest("rushi@example.com", "secret123");
        User existingUser = User.builder()
                .id(10L)
                .name("Rushikesh")
                .email("rushi@example.com")
                .password("encodedPassword123")
                .authProvider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("rushi@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("secret123", "encodedPassword123")).thenReturn(true);

        User result = userService.authenticateLocalUser(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("Create new Google OAuth user when user does not exist")
    void testProcessOAuthUser_NewUser() {
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-12345");
        when(oAuth2User.getAttribute("email")).thenReturn("testuser@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://example.com/photo.jpg");

        when(userRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser@example.com"))
                .thenReturn(Optional.empty());

        User createdUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("testuser@example.com")
                .authProvider(AuthProvider.GOOGLE)
                .providerId("google-sub-12345")
                .profileImageUrl("https://example.com/photo.jpg")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        User result = userService.processOAuthUser(oAuth2User);

        assertNotNull(result);
        assertEquals("testuser@example.com", result.getEmail());
        assertEquals(AuthProvider.GOOGLE, result.getAuthProvider());
        assertEquals("google-sub-12345", result.getProviderId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Lookup and update existing Google OAuth user without duplicate creation")
    void testProcessOAuthUser_ExistingUser() {
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-12345");
        when(oAuth2User.getAttribute("email")).thenReturn("testuser@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Updated Name");
        when(oAuth2User.getAttribute("picture")).thenReturn("https://example.com/photo.jpg");

        User existingUser = User.builder()
                .id(1L)
                .name("Old Name")
                .email("testuser@example.com")
                .authProvider(AuthProvider.GOOGLE)
                .providerId("google-sub-12345")
                .profileImageUrl("https://example.com/photo.jpg")
                .build();

        when(userRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-12345"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.processOAuthUser(oAuth2User);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Name", result.getName());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Throw exception when Google OAuth email is missing")
    void testProcessOAuthUser_MissingEmail() {
        when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-12345");
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> userService.processOAuthUser(oAuth2User));
        verify(userRepository, never()).save(any());
    }
}
