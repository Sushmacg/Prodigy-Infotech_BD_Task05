package com.example.usersapi.config;

import com.example.usersapi.model.Role;
import com.example.usersapi.model.User;
import com.example.usersapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Bootstraps a single initial ADMIN account on startup so there's a way into the
 * admin-only endpoints without already having an admin. Controlled entirely by
 * environment variables - if ADMIN_EMAIL/ADMIN_PASSWORD aren't set, or an admin
 * with that email already exists, this is a no-op.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.name:Admin}") String adminName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD not set - skipping admin seeding.");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user '{}' already exists - skipping seeding.", adminEmail);
            return;
        }

        User admin = new User(
                UUID.randomUUID(),
                adminName,
                adminEmail,
                30, // placeholder age - update via /api/users/{id} after login if desired
                passwordEncoder.encode(adminPassword),
                Role.ADMIN
        );

        userRepository.save(admin);
        log.info("Seeded initial admin account with email '{}'.", adminEmail);
    }
}
