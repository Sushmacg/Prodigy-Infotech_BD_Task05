package com.example.usersapi.controller;

import com.example.usersapi.dto.UserCreateRequest;
import com.example.usersapi.dto.UserResponse;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.model.User;
import com.example.usersapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// All endpoints here are restricted to ADMIN at the URL level in SecurityConfig.
// @PreAuthorize is added too as defense-in-depth in case the URL rule is ever changed.
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User created = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(UserResponse.fromEntity(created));
    }

    // READ - all
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // READ - one
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponse.fromEntity(userService.findById(id)));
    }

    // UPDATE (partial fields allowed)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        User updated = userService.update(id, request);
        return ResponseEntity.ok(UserResponse.fromEntity(updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
