package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("select count(oi.id) from OrderItem oi where oi.product.id = :productId")
    long countByProductId(@Param("productId") int productId);
}
