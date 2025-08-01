package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.*;
import com.stackpuz.example.backend.repository.WishlistRepository;
import com.stackpuz.example.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public Wishlist getUserWishlist(User user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    return wishlistRepository.save(newWishlist);
                });
    }

    public void addToWishlist(User user, Integer productId) {
        Wishlist wishlist = getUserWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Check if product already exists in wishlist
        boolean exists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct().getId() == productId); // Changed to use == for primitive comparison

        if (!exists) {
            wishlist.addItem(product);
            wishlistRepository.save(wishlist);
        }
    }

    public void removeFromWishlist(User user, Integer productId) {
        Wishlist wishlist = getUserWishlist(user);
        wishlist.getItems().removeIf(item -> item.getProduct().getId() == productId); // Changed to use ==
        wishlistRepository.save(wishlist);
    }

    public void clearWishlist(User user) {
        Wishlist wishlist = getUserWishlist(user);
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
    }

    public int getWishlistCount(User user) {
        Wishlist wishlist = wishlistRepository.findByUser(user).orElse(null);
        return wishlist != null ? wishlist.getItems().size() : 0;
    }
}