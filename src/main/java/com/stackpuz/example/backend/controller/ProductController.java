package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import com.stackpuz.example.backend.service.DiscountCodeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    private final DiscountCodeService discountCodeService;

    public ProductController(ProductService service, DiscountCodeService discountCodeService) {
        this.service = service;
        this.discountCodeService = discountCodeService;
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})  // Allow both ADMIN and VIEWER roles
    public String getProducts(Model model) {
        List<Product> products = service.getProducts();
        model.addAttribute("products", products);
        discountCodeService.getActiveBanner().ifPresent(dc -> model.addAttribute("activeDiscount", dc));
        model.addAttribute("activeDiscounts", discountCodeService.getAllActive());
        return "products";
    }

    // Provide API endpoint to fetch a product by ID for client-side modal usage
    @GetMapping("/api/{id}")
    @ResponseBody
    public Product getProductByIdApi(@PathVariable int id) {
        return service.getProductById(id);
    }

    @PostMapping("/api")
    @Secured("ROLE_ADMIN")  // Only ADMIN can create
    @ResponseBody
    public Product createProduct(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    @PutMapping("/api/{id}")
    @Secured("ROLE_ADMIN")  // Only ADMIN can update
    @ResponseBody
    public Product updateProduct(@PathVariable int id, @RequestBody Product product) {
        return service.updateProduct(id, product);
    }

    @DeleteMapping("/api/{id}")
    @Secured("ROLE_ADMIN")  // Only ADMIN can delete
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            service.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})
    public String searchProducts(
            @RequestParam(required = false) String query,
            Model model) {

        if (query == null || query.trim().isEmpty()) {
            return "redirect:/products";
        }

        List<Product> products;
        try {
            // Try to parse as ID first
            int id = Integer.parseInt(query);
            products = List.of(service.getProductById(id));
            model.addAttribute("searchId", id);
        } catch (NumberFormatException | EntityNotFoundException e) {
            // If not a number or no product with that ID, search by name
            products = service.searchProductsByName(query);
            model.addAttribute("searchName", query);
        }

        model.addAttribute("products", products);
        return "products";
    }
}