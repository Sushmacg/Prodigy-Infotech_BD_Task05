package com.example.usersapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UserUpdateRequest {

    private String name;

    @Email(message = "email must be a valid email address")
    private String email;

    @Min(value = 0, message = "age must be greater than or equal to 0")
    @Max(value = 150, message = "age must be realistic (<= 150)")
    private Integer age;

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
}
