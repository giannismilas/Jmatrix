package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.Order;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.CartRepository;
import com.stackpuz.example.backend.repository.OrderRepository;
import com.stackpuz.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("john_doe");
        testUser.setPassword("test123");
        testUser.setRole("ROLE_USER");
        testUser.setFullName("John Doe");
        testUser.setAddress("123 Main Street");
        testUser.setPhone("1234567890");
        testUser.setEmail("john@example.com");
        testUser.setBio("Test bio");

        testUser = userService.registerUser(testUser.getUsername(), testUser.getPassword());
    }

    @Test
    void testRegisterUser() {
        User newUser = userService.registerUser("newuser", "pass123");
        assertNotNull(newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertTrue(passwordEncoder.matches("pass123", newUser.getPassword()));
    }

    @Test
    void testUsernameExists() {
        assertTrue(userService.usernameExists("john_doe"));
        assertFalse(userService.usernameExists("nonexistent_user"));
    }

    @Test
    void testFindByUsername() {
        Optional<User> found = userService.findByUsername("john_doe");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().getUsername());
    }

    @Test
    void testLoadUserByUsername() {
        var details = userService.loadUserByUsername("john_doe");
        assertEquals("john_doe", details.getUsername());
    }

    @Test
    void testLoadUserByUsernameThrowsIfNotFound() {
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("not_existing_user"));
    }

    @Test
    void testGetAllUsers() {
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void testUpdateProfile() {
        User updated = new User();
        updated.setFullName("Updated Name");
        updated.setAddress("New Address");
        updated.setPhone("9999999999");
        updated.setEmail("updated@example.com");
        updated.setBio("Updated bio");

        userService.updateProfile(testUser.getId(), updated);
        User found = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("Updated Name", found.getFullName());
        assertEquals("New Address", found.getAddress());
    }

    @Test
    void testGetUserOrderCount() {
        long count = userService.getUserOrderCount(testUser.getId());
        assertEquals(0, count);

        Order order = new Order();
        order.setUser(testUser);
        orderRepository.save(order);

        assertEquals(1, userService.getUserOrderCount(testUser.getId()));
    }

    @Test
    void testDeleteUser() {
        Cart cart = new Cart();
        cart.setUser(testUser);
        cartRepository.save(cart);

        Order order = new Order();
        order.setUser(testUser);
        orderRepository.save(order);

        userService.deleteUser(testUser.getId());

        assertFalse(userRepository.findById(testUser.getId()).isPresent());
    }

    @Test
    void testDeleteUserThrowsIfAdmin() {
        final User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole("ROLE_ADMIN");
        User savedAdmin = userRepository.save(admin);

        final Long adminId = savedAdmin.getId();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(adminId)
        );

    }

    @Test
    void testDeleteUserThrowsIfNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(999L));
    }
}
