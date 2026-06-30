package com.example.usersapi;

import com.example.usersapi.dto.UserCreateRequest;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.exception.UserNotFoundException;
import com.example.usersapi.model.User;
import com.example.usersapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UsersApiApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    void createsFindsUpdatesAndDeletesUser() {
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setName("Alice");
        createRequest.setEmail("alice@example.com");
        createRequest.setPassword("password123");
        createRequest.setAge(30);

        User created = userService.create(createRequest);
        assertThat(created.getId()).isNotNull();

        User found = userService.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Alice");

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setAge(31);
        User updated = userService.update(created.getId(), updateRequest);
        assertThat(updated.getAge()).isEqualTo(31);

        userService.delete(created.getId());
        assertThatThrownBy(() -> userService.findById(created.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        UserCreateRequest req1 = new UserCreateRequest();
        req1.setName("Bob");
        req1.setEmail("bob@example.com");
        req1.setPassword("password123");
        req1.setAge(25);
        userService.create(req1);

        UserCreateRequest req2 = new UserCreateRequest();
        req2.setName("Bobby");
        req2.setEmail("bob@example.com");
        req2.setPassword("password456");
        req2.setAge(40);

        assertThatThrownBy(() -> userService.create(req2))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void throwsNotFoundForUnknownId() {
        assertThatThrownBy(() -> userService.findById(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);
    }
}
