package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        createAdminIfNotExists(); // Add this line to create admin user on startup
    }

    private void createAdminIfNotExists() {
        if (!usernameExists("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin")); // Change this password in production
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            log.info("Admin user created");
        }
    }

    public User registerUser(String username, String password) {
        log.debug("Attempting to register new user: {}", username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);
        log.debug("User registered with role: {}", savedUser.getRole());
        return savedUser;
    }

    public boolean usernameExists(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.debug("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }
        log.debug("User found: {} with role: {}", username, user.getRole());
        String role = user.getRole().replace("ROLE_", "");
        log.debug("Mapped role for Spring Security: {}", role);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .build();
    }
}