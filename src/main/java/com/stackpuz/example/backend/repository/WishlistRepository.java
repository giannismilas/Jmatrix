package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.Wishlist;
import com.stackpuz.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    Optional<Wishlist> findByUser(User user);
}