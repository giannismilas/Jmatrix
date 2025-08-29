package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.DiscountCode;
import com.stackpuz.example.backend.repository.DiscountCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiscountCodeService {
    private final DiscountCodeRepository repository;

    public DiscountCodeService(DiscountCodeRepository repository) {
        this.repository = repository;
    }

    public DiscountCode create(String code, double percent, LocalDateTime expiresAt) {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        DiscountCode dc = new DiscountCode();
        dc.setCode(code.trim());
        dc.setPercent(percent);
        dc.setActive(true);
        dc.setExpiresAt(expiresAt);
        return repository.save(dc);
    }

    public Optional<DiscountCode> findByCode(String code) {
        return repository.findByCodeIgnoreCase(code);
    }

    public DiscountCode requireValidActive(String code) {
        DiscountCode dc = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("Invalid discount code"));
        if (!dc.isCurrentlyActive()) {
            throw new IllegalStateException("Discount code is not active");
        }
        if (dc.getPercent() <= 0 || dc.getPercent() > 100) {
            throw new IllegalStateException("Discount percent is invalid");
        }
        return dc;
    }

    public Optional<DiscountCode> getActiveBanner() {
        return repository.findFirstByActiveTrueOrderByIdDesc()
                .filter(DiscountCode::isCurrentlyActive);
    }

    public List<DiscountCode> getAllActive() {
        return repository.findAllByActiveTrueOrderByIdDesc()
                .stream()
                .filter(DiscountCode::isCurrentlyActive)
                .collect(Collectors.toList());
    }

    // Auto-delete expired codes every hour
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredCodes() {
        repository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
