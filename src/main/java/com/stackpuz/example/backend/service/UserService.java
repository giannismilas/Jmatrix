package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {
        if (!usernameExists("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
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
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.debug("User not found with username: {}", username);
                return new UsernameNotFoundException("User not found");
            });
        
        log.debug("User found: {} with role: {}", username, user.getRole());
        String role = user.getRole().replace("ROLE_", "");
        log.debug("Mapped role for Spring Security: {}", role);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .build();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Cannot delete admin user");
        }
        
        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);
    }
}