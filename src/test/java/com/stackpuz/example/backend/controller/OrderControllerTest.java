package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Order;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.enums.OrderStatus;
import com.stackpuz.example.backend.service.OrderService;
import com.stackpuz.example.backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private OrderController orderController;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PLACED);
    }

    @Test
    void createOrder_WhenUserExists_CreatesOrderSuccessfully() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrderFromCart(testUser)).thenReturn(testOrder);

        // Act
        ResponseEntity<Order> response = orderController.createOrder(userDetails);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testOrder, response.getBody());
        verify(orderService).createOrderFromCart(testUser);
    }

    @Test
    void createOrder_WhenUserNotFound_ReturnsBadRequest() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("unknownUser");
        when(userService.findByUsername("unknownUser")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Order> response = orderController.createOrder(userDetails);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void createOrder_WhenServiceThrowsException_ReturnsBadRequest() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(orderService.createOrderFromCart(testUser)).thenThrow(new IllegalStateException("Empty cart"));

        // Act
        ResponseEntity<Order> response = orderController.createOrder(userDetails);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void updateOrderStatus_WhenOrderExists_UpdatesStatusSuccessfully() {
        // Arrange
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setStatus(OrderStatus.SHIPPED);

        when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED)).thenReturn(updatedOrder);

        // Act
        ResponseEntity<Order> response = orderController.updateOrderStatus(1L, OrderStatus.SHIPPED);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(OrderStatus.SHIPPED, response.getBody().getStatus());
    }

    @Test
    void updateOrderStatus_WhenOrderNotFound_ReturnsBadRequest() {
        // Arrange
        when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPED))
                .thenThrow(new EntityNotFoundException("Order not found"));

        // Act
        ResponseEntity<Order> response = orderController.updateOrderStatus(1L, OrderStatus.SHIPPED);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void deleteOrder_WhenOrderExists_DeletesSuccessfully() {
        // Arrange - No need to mock anything since we're testing void method

        // Act
        ResponseEntity<Void> response = orderController.deleteOrder(1L);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(orderService).deleteOrder(1L);
    }

    @Test
    void deleteOrder_WhenOrderNotFound_ReturnsBadRequest() {
        // Arrange
        doThrow(new EntityNotFoundException("Order not found"))
                .when(orderService).deleteOrder(1L);

        // Act
        ResponseEntity<Void> response = orderController.deleteOrder(1L);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }
}