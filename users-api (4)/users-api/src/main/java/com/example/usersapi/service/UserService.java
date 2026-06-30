package com.example.usersapi.service;

import com.example.usersapi.dto.UserCreateRequest;
import com.example.usersapi.dto.UserUpdateRequest;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.exception.UserNotFoundException;
import com.example.usersapi.model.Role;
import com.example.usersapi.model.User;
import com.example.usersapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    // Admin-only path (enforced at the controller/security layer) - lets an admin create
    // any user, including other admins, with a chosen role.
    @Transactional
    public User create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("A user with email '" + request.getEmail() + "' already exists");
        }

        Role role = parseRole(request.getRole());

        User user = new User(
                UUID.randomUUID(),
                request.getName(),
                request.getEmail(),
                request.getAge(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        return userRepository.save(user);
    }

    @Transactional
    public User update(UUID id, UserUpdateRequest request) {
        User existing = findById(id); // throws 404 if absent

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateEmailException("A user with email '" + request.getEmail() + "' already exists");
            }
            existing.setEmail(request.getEmail());
        }
        if (request.getAge() != null) {
            existing.setAge(request.getAge());
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private Role parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return Role.USER;
        }
        try {
            return Role.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("role must be one of: USER, ADMIN");
        }
    }
}
