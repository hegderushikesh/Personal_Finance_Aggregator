package com.finpilot.auth.dto;

import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private AuthProvider authProvider;
    private String profileImageUrl;

    public static UserResponse from(User user) {
        Role role = user.getRole() != null ? user.getRole() : Role.USER;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(role.name())
                .authProvider(user.getAuthProvider())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
