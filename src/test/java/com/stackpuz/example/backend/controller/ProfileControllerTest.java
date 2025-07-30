package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private ProfileController profileController;

    private User testUser;
    private User updatedUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
    }

    @Test
    void viewProfile_WhenUserAuthenticated_ReturnsProfileViewWithUserData() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.getUserOrderCount(testUser.getId())).thenReturn(5L);

        // Act
        String viewName = profileController.viewProfile(model);

        // Assert
        assertEquals("profile", viewName);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("orderCount", 5L);
        verify(userService).getCurrentUser();
        verify(userService).getUserOrderCount(testUser.getId());
    }

    @Test
    void viewProfile_WhenUserNotAuthenticated_RedirectsToLogin() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(null);

        // Act
        String viewName = profileController.viewProfile(model);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(model);
    }

    @Test
    void updateProfile_WhenUserAuthenticated_UpdatesProfileAndRedirects() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        String redirect = profileController.updateProfile(updatedUser);

        // Assert
        assertEquals("redirect:/profile?success", redirect);
        verify(userService).getCurrentUser();
        verify(userService).updateProfile(testUser.getId(), updatedUser);
    }

    @Test
    void updateProfile_WhenUserNotAuthenticated_RedirectsToLogin() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(null);

        // Act
        String redirect = profileController.updateProfile(updatedUser);

        // Assert
        assertEquals("redirect:/login", redirect);
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteProfile_WhenUserAuthenticated_DeletesProfileAndReturnsSuccess() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        ResponseEntity<String> response = profileController.deleteProfile();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile deleted successfully", response.getBody());
        verify(userService).getCurrentUser();
        verify(userService).deleteUser(testUser.getId());
        // Verify security context was cleared
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deleteProfile_WhenUserNotAuthenticated_ReturnsUnauthorized() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(null);

        // Act
        ResponseEntity<String> response = profileController.deleteProfile();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated", response.getBody());
        verify(userService).getCurrentUser();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteProfile_WhenServiceThrowsException_ReturnsBadRequest() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        doThrow(new RuntimeException("Database error")).when(userService).deleteUser(testUser.getId());

        // Act
        ResponseEntity<String> response = profileController.deleteProfile();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().startsWith("Error deleting profile:"));
        verify(userService).getCurrentUser();
        verify(userService).deleteUser(testUser.getId());
    }
}