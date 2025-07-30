package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setRole("ROLE_USER");

        user2 = new User();
        user2.setUsername("user2");
        user2.setRole("ROLE_USER");
    }

    @Test
    void getUsers_ShouldReturnUsersViewWithAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        String viewName = userController.getUsers(model);

        // Assert
        assertEquals("users", viewName);
        verify(model).addAttribute("users", users);
        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnOk() {
        // Arrange - no need to mock anything for successful case

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertEquals(ResponseEntity.ok().build(), response);
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldReturnNotFound() {
        // Arrange
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).deleteUser(1L);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_WhenTryingToDeleteAdmin_ShouldReturnBadRequest() {
        // Arrange
        doThrow(new IllegalArgumentException("Cannot delete admin user"))
                .when(userService).deleteUser(1L);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertEquals(ResponseEntity.badRequest().build(), response);
        verify(userService).deleteUser(1L);
    }
}