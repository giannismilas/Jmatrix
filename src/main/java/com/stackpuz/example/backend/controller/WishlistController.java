package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.entity.Wishlist;
import com.stackpuz.example.backend.repository.UserRepository;
import com.stackpuz.example.backend.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    public WishlistController(WishlistService wishlistService, UserRepository userRepository) {
        this.wishlistService = wishlistService;
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
    public String viewWishlist(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/products";
        }

        Wishlist wishlist = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlist", wishlist);
        return "wishlist";
    }

    @PostMapping("/add/{productId}")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@PathVariable Integer productId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to add items to wishlist");
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admins cannot use wishlist functionality");
        }

        wishlistService.addToWishlist(user, productId);
        return ResponseEntity.ok("Product added to wishlist");
    }

    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<String> removeFromWishlist(@PathVariable Integer productId) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to remove items from wishlist");
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admins cannot use wishlist functionality");
        }

        wishlistService.removeFromWishlist(user, productId);
        return ResponseEntity.ok("Product removed from wishlist");
    }
}