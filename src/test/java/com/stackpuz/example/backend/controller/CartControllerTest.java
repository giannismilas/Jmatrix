package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.service.CartService;
import com.stackpuz.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CartController cartController;

    private User testUser;
    private User adminUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setRole("ROLE_USER");

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole("ROLE_ADMIN");

        testCart = new Cart();
        testCart.setUser(testUser);
    }

    @Test
    void viewCart_WhenUserNotLoggedIn_RedirectsToLogin() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        String result = cartController.viewCart(model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void viewCart_WhenUserIsAdmin_RedirectsToProducts() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        SecurityContextHolder.setContext(securityContext);

        String result = cartController.viewCart(model);

        assertEquals("redirect:/products", result);
    }

    @Test
    void viewCart_WhenValidUser_ReturnsCartView() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(cartService.getUserCart(testUser)).thenReturn(testCart);
        SecurityContextHolder.setContext(securityContext);

        String result = cartController.viewCart(model);

        assertEquals("cart", result);
        verify(model).addAttribute("cart", testCart);
        verify(model).addAttribute("totalPrice", testCart.getTotalPrice());
    }

    @Test
    void addToCart_WhenUserNotLoggedIn_ReturnsUnauthorized() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.addToCart(1, 1);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Please log in to add items to cart", response.getBody());
    }

    @Test
    void addToCart_WhenUserIsAdmin_ReturnsForbidden() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.addToCart(1, 1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Admins cannot use cart functionality", response.getBody());
    }

    @Test
    void addToCart_WhenValidUser_AddsProductToCart() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.addToCart(1, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product added to cart", response.getBody());
        verify(cartService).addToCart(testUser, 1, 2);
    }

    @Test
    void removeFromCart_WhenUserNotLoggedIn_ReturnsUnauthorized() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.removeFromCart(1);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Please log in to remove items from cart", response.getBody());
    }

    @Test
    void removeFromCart_WhenUserIsAdmin_ReturnsForbidden() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.removeFromCart(1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Admins cannot use cart functionality", response.getBody());
    }

    @Test
    void removeFromCart_WhenValidUser_RemovesProductFromCart() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cartController.removeFromCart(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product removed from cart", response.getBody());
        verify(cartService).removeFromCart(testUser, 1);
    }
}