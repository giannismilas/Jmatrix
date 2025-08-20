package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.Review;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.ProductRepository;
import com.stackpuz.example.backend.repository.ReviewRepository;
import com.stackpuz.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setName("Phone");
        product.setPrice(10.0);

        user = new User();
        user.setId(100L);
        user.setUsername("john");
        user.setRole("ROLE_USER");
    }

    @Test
    void addOrUpdateReview_createsNew_whenNotExists() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductAndUser(product, user)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(55L);
            return r;
        });

        Review r = reviewService.addOrUpdateReview(1, "john", 5, "great");

        assertNotNull(r.getId());
        assertEquals(5, r.getRating());
        assertEquals("great", r.getComment());
        assertEquals(product, r.getProduct());
        assertEquals(user, r.getUser());
    }

    @Test
    void addOrUpdateReview_updatesExisting_whenExists() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        Review existing = new Review();
        existing.setId(77L);
        existing.setProduct(product);
        existing.setUser(user);
        existing.setRating(3);
        when(reviewRepository.findByProductAndUser(product, user)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review r = reviewService.addOrUpdateReview(1, "john", 4, "updated");

        assertEquals(77L, r.getId());
        assertEquals(4, r.getRating());
        assertEquals("updated", r.getComment());
    }

    @Test
    void addOrUpdateReview_throwsOnInvalidRating() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.addOrUpdateReview(1, "john", 0, "bad"));
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.addOrUpdateReview(1, "john", 6, "bad"));
    }

    @Test
    void addOrUpdateReview_throwsWhenProductOrUserMissing() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> reviewService.addOrUpdateReview(1, "john", 5, null));

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> reviewService.addOrUpdateReview(1, "john", 5, null));
    }

    @Test
    void getAverageRating_nullBecomesZero() {
        when(reviewRepository.getAverageRating(1)).thenReturn(null);
        assertEquals(0.0, reviewService.getAverageRating(1));
    }

    @Test
    void getReviewCount_nullBecomesZero() {
        when(reviewRepository.getReviewCount(1)).thenReturn(null);
        assertEquals(0L, reviewService.getReviewCount(1));
    }


}
