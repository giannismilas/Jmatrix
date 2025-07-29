package com.stackpuz.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String role;
    
    // New fields for profile
    private String fullName;
    private String address;
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "email")
    private String email;
}