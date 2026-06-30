package com.example.usersapi.controller;

import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.model.User;
import com.example.usersapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Returns the currently authenticated user's own profile - no path id, derived from the JWT
    @GetMapping
    public ResponseEntity<UserResponse> getOwnProfile(@AuthenticationPrincipal User currentUser) {
        User user = userService.findById(currentUser.getId());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    // Lets the authenticated user update their own name/email/age (not role - that's admin-only)
    @PutMapping
    public ResponseEntity<UserResponse> updateOwnProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        User updated = userService.update(currentUser.getId(), request);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }
}
