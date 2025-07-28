package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Order;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.enums.OrderStatus;
import com.stackpuz.example.backend.service.OrderService;
import com.stackpuz.example.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
@Controller
@RequestMapping("/orders")
public class OrderWebController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderWebController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewOrders(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            orders = orderService.getAllOrders();
            model.addAttribute("isAdmin", true);
        } else {
            orders = orderService.getUserOrders(user);
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());  // Add this line
        return "orders";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return userService.findByUsername(auth.getName()).orElse(null);
    }
}