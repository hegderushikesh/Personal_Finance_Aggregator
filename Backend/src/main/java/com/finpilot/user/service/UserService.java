package com.finpilot.user.service;

import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import com.finpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

import com.finpilot.auth.dto.LoginRequest;
import com.finpilot.auth.dto.SignUpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User registerLocalUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .providerId(null)
                .profileImageUrl(null)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Registered new local user ID: {} with email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User authenticateLocalUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (user.getPassword() == null) {
            throw new BadCredentialsException("This account uses Google OAuth login. Please continue with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        return user;
    }

    @Transactional
    public User processOAuthUser(OAuth2User oauthUser) {
        String providerId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        if (!StringUtils.hasText(email)) {
            log.error("Google OAuth2 login failed: email attribute missing from OAuth2User attributes.");
            throw new IllegalArgumentException("OAuth2 provider did not return an email address.");
        }

        if (!StringUtils.hasText(name)) {
            name = email.split("@")[0];
        }

        // 1. Try to find user by AuthProvider (GOOGLE) and providerId
        Optional<User> userByProvider = userRepository.findByAuthProviderAndProviderId(AuthProvider.GOOGLE, providerId);

        if (userByProvider.isPresent()) {
            User existingUser = userByProvider.get();
            // Update safe profile fields
            existingUser.setName(name);
            if (StringUtils.hasText(picture)) {
                existingUser.setProfileImageUrl(picture);
            }
            log.info("Updated existing Google OAuth user ID: {}", existingUser.getId());
            return userRepository.save(existingUser);
        }

        // 2. Check if user exists by email
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User existingUser = userByEmail.get();
            existingUser.setAuthProvider(AuthProvider.GOOGLE);
            existingUser.setProviderId(providerId);
            existingUser.setName(name);
            if (StringUtils.hasText(picture)) {
                existingUser.setProfileImageUrl(picture);
            }
            log.info("Linked existing user email {} to Google OAuth provider ID {}", email, providerId);
            return userRepository.save(existingUser);
        }

        // 3. Create new user
        User newUser = User.builder()
                .name(name)
                .email(email)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .profileImageUrl(picture)
                .password(null)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Created new Google OAuth user ID: {} with email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }
}
