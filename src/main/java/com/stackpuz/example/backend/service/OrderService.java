package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.Order;
import com.stackpuz.example.backend.entity.OrderItem;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.enums.OrderStatus;
import com.stackpuz.example.backend.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, 
                       CartService cartService,
                       UserService userService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userService = userService;
    }

    public Order createOrderFromCart(User user) {
        Cart cart = cartService.getUserCart(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getTotalPrice());

        // Convert cart items to order items
        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPriceAtOrder(cartItem.getProduct().getPrice());
                return orderItem;
            })
            .collect(Collectors.toList());

        order.setItems(orderItems);
        
        // Save the order
        Order savedOrder = orderRepository.save(order);
        
        // Clear the cart
        cartService.clearCart(user);
        
        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        orderRepository.delete(order);
    }

}