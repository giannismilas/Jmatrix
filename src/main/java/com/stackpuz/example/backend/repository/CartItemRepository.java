package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Modifying
    @Query("delete from CartItem ci where ci.product.id = :productId")
    void deleteByProductId(@Param("productId") int productId);
}
