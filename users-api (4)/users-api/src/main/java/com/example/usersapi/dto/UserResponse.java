package com.example.usersapi.dto;

import com.example.usersapi.model.User;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Integer age;
    private String role;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, Integer age, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getRole().name()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
