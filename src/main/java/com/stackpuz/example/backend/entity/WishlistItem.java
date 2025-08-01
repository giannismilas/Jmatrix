package com.stackpuz.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Wishlist wishlist;

    @ManyToOne
    private Product product;
}