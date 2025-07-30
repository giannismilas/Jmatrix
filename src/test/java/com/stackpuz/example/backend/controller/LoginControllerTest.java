package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private LoginController loginController;

    @Test
    void login_ShouldReturnLoginView() {
        // Act
        String viewName = loginController.login();

        // Assert
        assertEquals("login", viewName);
    }

    @Test
    void showRegistrationForm_ShouldReturnRegisterView() {
        // Act
        String viewName = loginController.showRegistrationForm();

        // Assert
        assertEquals("register", viewName);
    }

    @Test
    void registerUser_WhenPasswordsDontMatch_ShouldReturnRegisterWithError() {
        // Act
        String viewName = loginController.registerUser(
                "testuser",
                "password1",
                "password2",
                model
        );

        // Assert
        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verifyNoInteractions(userService);
    }

    @Test
    void registerUser_WhenUsernameExists_ShouldReturnRegisterWithError() {
        // Arrange
        when(userService.usernameExists("existinguser")).thenReturn(true);

        // Act
        String viewName = loginController.registerUser(
                "existinguser",
                "password",
                "password",
                model
        );

        // Assert
        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Username already exists");
        verify(userService).usernameExists("existinguser");
        verifyNoMoreInteractions(userService);
    }

    @Test
    void registerUser_WhenValidInput_ShouldRegisterAndRedirect() {
        // Arrange
        when(userService.usernameExists("newuser")).thenReturn(false);

        // Act
        String viewName = loginController.registerUser(
                "newuser",
                "password",
                "password",
                model
        );

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(userService).usernameExists("newuser");
        verify(userService).registerUser("newuser", "password");
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(model);
    }
}