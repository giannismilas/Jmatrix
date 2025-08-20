package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.Review;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.ProductRepository;
import com.stackpuz.example.backend.repository.ReviewRepository;
import com.stackpuz.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Review> getReviewsForProduct(int productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Double getAverageRating(int productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        return avg == null ? 0.0 : avg;
    }

    public Long getReviewCount(int productId) {
        Long count = reviewRepository.getReviewCount(productId);
        return count == null ? 0L : count;
    }

    public Review addOrUpdateReview(int productId, String username, int rating, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = reviewRepository.findByProductAndUser(product, user).orElseGet(Review::new);
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public Review updateReview(Long reviewId, String username, int rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new SecurityException("You can only edit your own review");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own review");
        }
        reviewRepository.delete(review);
    }
}
