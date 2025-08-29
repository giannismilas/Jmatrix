package com.stackpuz.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // Discount application (optional, percentage 0..100)
    private String appliedDiscountCode;
    private Double appliedDiscountPercent; // null means no discount

    public double getSubtotal() {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public double getDiscountAmount() {
        if (appliedDiscountPercent == null || appliedDiscountPercent <= 0) return 0.0;
        return getSubtotal() * (appliedDiscountPercent / 100.0);
    }

    public double getTotalPrice() {
        double total = getSubtotal() - getDiscountAmount();
        return Math.max(total, 0.0);
    }
}