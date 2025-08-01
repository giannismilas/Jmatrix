package com.stackpuz.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishlistItem> items = new ArrayList<>();

    public void addItem(Product product) {
        WishlistItem item = new WishlistItem();
        item.setWishlist(this);
        item.setProduct(product);
        items.add(item);
    }
}