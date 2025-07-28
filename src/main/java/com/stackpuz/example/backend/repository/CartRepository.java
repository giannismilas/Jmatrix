package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUser(User user);
}