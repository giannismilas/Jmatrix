package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.DiscountCode;
import com.stackpuz.example.backend.service.DiscountCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/discount-codes")
public class DiscountCodeController {
    private final DiscountCodeService discountCodeService;

    public DiscountCodeController(DiscountCodeService discountCodeService) {
        this.discountCodeService = discountCodeService;
    }

    @PostMapping("/api")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<DiscountCode> create(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        Number percentNum = (Number) payload.get("percent");
        String expiresAtStr = (String) payload.get("expiresAt");
        if (code == null || percentNum == null) {
            return ResponseEntity.badRequest().build();
        }
        double percent = percentNum.doubleValue();
        LocalDateTime expiresAt = null;
        if (expiresAtStr != null && !expiresAtStr.isBlank()) {
            try {
                expiresAt = LocalDateTime.parse(expiresAtStr);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        DiscountCode created = discountCodeService.create(code, percent, expiresAt);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/api/active")
    public ResponseEntity<DiscountCode> getActive() {
        return discountCodeService.getActiveBanner()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/api/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            // If already expired, repository call in service scheduler may have removed it; deleteById is idempotent
            discountCodeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
