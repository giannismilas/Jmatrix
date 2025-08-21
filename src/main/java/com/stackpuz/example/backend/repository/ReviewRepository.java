package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.Review;
import com.stackpuz.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);

    @Query("select r from Review r where r.product.id = :productId order by r.createdAt desc")
    List<Review> findByProductId(@Param("productId") int productId);

    Optional<Review> findByProductAndUser(Product product, User user);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double getAverageRating(@Param("productId") int productId);

    @Query("select count(r.id) from Review r where r.product.id = :productId")
    Long getReviewCount(@Param("productId") int productId);

    @Modifying
    @Query("delete from Review r where r.product.id = :productId")
    void deleteByProductId(@Param("productId") int productId);
}
