package com.example.usersapi.service;

import com.example.usersapi.dto.AuthResponse;
import com.example.usersapi.dto.LoginRequest;
import com.example.usersapi.dto.RegisterRequest;
import com.example.usersapi.exception.DuplicateEmailException;
import com.example.usersapi.model.Role;
import com.example.usersapi.model.User;
import com.example.usersapi.repository.UserRepository;
import com.example.usersapi.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("A user with email '" + request.getEmail() + "' already exists");
        }

        User user = new User(
                UUID.randomUUID(),
                request.getName(),
                request.getEmail(),
                request.getAge(),
                passwordEncoder.encode(request.getPassword()), // hash before storing - never store raw passwords
                Role.USER // all self-registrations default to USER; promotion to ADMIN is a separate admin action
        );

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getEmail(), saved.getAuthorities());
        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getAuthorities());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}
