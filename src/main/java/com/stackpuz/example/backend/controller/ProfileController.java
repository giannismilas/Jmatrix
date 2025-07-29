package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("orderCount", userService.getUserOrderCount(user.getId()));
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        userService.updateProfile(currentUser.getId(), updatedUser);
        return "redirect:/profile?success";
    }

    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteProfile() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        
        try {
            userService.deleteUser(currentUser.getId());
            SecurityContextHolder.clearContext(); // Clear security context to logout user
            return ResponseEntity.ok("Profile deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting profile: " + e.getMessage());
        }
    }
}