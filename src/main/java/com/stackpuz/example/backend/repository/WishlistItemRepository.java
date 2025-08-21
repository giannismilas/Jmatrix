package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Integer> {
    @Modifying
    @Query("delete from WishlistItem wi where wi.product.id = :productId")
    void deleteByProductId(@Param("productId") int productId);
}
