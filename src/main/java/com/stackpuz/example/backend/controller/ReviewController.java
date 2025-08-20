package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Review;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.service.ReviewService;
import com.stackpuz.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/product/{productId}")
    public Map<String, Object> getReviewsForProduct(@PathVariable int productId) {
        List<Review> reviews = reviewService.getReviewsForProduct(productId);
        Double average = reviewService.getAverageRating(productId);
        Long count = reviewService.getReviewCount(productId);

        User me = currentUser();
        List<Map<String, Object>> items = reviews.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("username", r.getUser().getUsername());
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("createdAt", r.getCreatedAt());
            m.put("mine", me != null && r.getUser().getId().equals(me.getId()));
            return m;
        }).toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("average", average);
        payload.put("count", count);
        payload.put("items", items);
        return payload;
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<?> addOrUpdateReview(@PathVariable int productId,
                                               @RequestParam int rating,
                                               @RequestParam(required = false) String comment) {
        User me = currentUser();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login to submit a review");
        }
        if ("ROLE_ADMIN".equals(me.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admins cannot create or edit reviews");
        }
        try {
            Review r = reviewService.addOrUpdateReview(productId, me.getUsername(), rating, comment);
            return ResponseEntity.ok(r.getId());
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @RequestParam int rating,
                                          @RequestParam(required = false) String comment) {
        User me = currentUser();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
        }
        if ("ROLE_ADMIN".equals(me.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admins cannot edit reviews");
        }
        try {
            reviewService.updateReview(reviewId, me.getUsername(), rating, comment);
            return ResponseEntity.ok().build();
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        User me = currentUser();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
        }
        if ("ROLE_ADMIN".equals(me.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admins cannot delete reviews");
        }
        try {
            reviewService.deleteReview(reviewId, me.getUsername());
            return ResponseEntity.ok().build();
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
