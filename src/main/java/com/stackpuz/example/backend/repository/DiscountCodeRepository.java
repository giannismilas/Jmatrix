package com.stackpuz.example.backend.repository;

import com.stackpuz.example.backend.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    Optional<DiscountCode> findFirstByActiveTrueOrderByIdDesc();
    List<DiscountCode> findAllByActiveTrueOrderByIdDesc();
    void deleteAllByExpiresAtBefore(LocalDateTime time);
}
