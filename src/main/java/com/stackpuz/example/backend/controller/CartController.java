package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.UserRepository;
import com.stackpuz.example.backend.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String viewCart(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/products";
        }
        
        Cart cart = cartService.getUserCart(user);
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "cart";
    }

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToCart(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to add items to cart");
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admins cannot use cart functionality");
        }
        
        cartService.addToCart(user, productId, quantity);
        return ResponseEntity.ok("Product added to cart");
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<String> removeFromCart(@PathVariable Integer productId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to remove items from cart");
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admins cannot use cart functionality");
        }
        
        cartService.removeFromCart(user, productId);
        return ResponseEntity.ok("Product removed from cart");
    }
}