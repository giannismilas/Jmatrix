package com.stackpuz.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    // percentage between 0 and 100
    @Column(nullable = false)
    private double percent;

    // Optional activation and expiration
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;

    // Convenience active flag; if null, treat as true when date windows allow
    private Boolean active = true;

    public boolean isCurrentlyActive() {
        if (Boolean.FALSE.equals(active)) return false;
        LocalDateTime now = LocalDateTime.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (expiresAt != null && now.isAfter(expiresAt)) return false;
        return true;
    }
}
