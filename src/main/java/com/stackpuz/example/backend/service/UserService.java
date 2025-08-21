package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.Order;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.entity.Wishlist;
import com.stackpuz.example.backend.repository.CartRepository;
import com.stackpuz.example.backend.repository.OrderRepository;
import com.stackpuz.example.backend.repository.UserRepository;
import com.stackpuz.example.backend.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository; // Add this
    private final WishlistRepository wishlistRepository;

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      OrderRepository orderRepository,
                      CartRepository cartRepository,
                      WishlistRepository wishlistRepository) { // Add parameter
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository; // Add assignment
        this.wishlistRepository = wishlistRepository;
        createAdminIfNotExists();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
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

public void updateProfile(Long userId, User updatedUser) {
    User existingUser = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    existingUser.setFullName(updatedUser.getFullName());
    existingUser.setAddress(updatedUser.getAddress());
    existingUser.setPhone(updatedUser.getPhone());
    existingUser.setEmail(updatedUser.getEmail());
    existingUser.setBio(updatedUser.getBio());
    
    userRepository.save(existingUser);
}

public User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
        return null;
    }
    return findByUsername(auth.getName()).orElse(null);
}

public long getUserOrderCount(Long userId) {
    return orderRepository.countByUserId(userId);
}
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Cannot delete admin user");
        }

        // Delete related orders first (if they exist)
        List<Order> userOrders = orderRepository.findByUserOrderByOrderDateDesc(user);
        orderRepository.deleteAll(userOrders);

        // Delete user's cart if it exists
        Cart userCart = cartRepository.findByUser(user).orElse(null);
        if (userCart != null) {
            cartRepository.delete(userCart);
        }

        // Delete user's wishlist if it exists
        Wishlist wishlist = wishlistRepository.findByUser(user).orElse(null);
        if (wishlist != null) {
            wishlistRepository.delete(wishlist);
        }

        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);
    }
}