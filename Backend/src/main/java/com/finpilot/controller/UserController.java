package com.finpilot.controller;

import com.finpilot.auth.dto.UserResponse;
import com.finpilot.security.UserPrincipal;
import com.finpilot.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        User user = currentUser.getUser();
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
