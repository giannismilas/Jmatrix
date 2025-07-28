package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Controller
@RequestMapping("/products") // Change this from /api/products
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public String getProducts(Model model) {
        List<Product> products = service.getProducts();
        model.addAttribute("products", products);
        return "products"; // This will look for products.html in src/main/resources/templates/
    }

    // API endpoints for CRUD operations
    @PostMapping("/api")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Product createProduct(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    @PutMapping("/api/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Product updateProduct(@PathVariable int id, @RequestBody Product product) {
        return service.updateProduct(id, product);
    }

    @DeleteMapping("/api/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public void deleteProduct(@PathVariable int id) {
        service.deleteProduct(id);
    }

    @DeleteMapping("/api")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public void deleteAllProducts() {
        service.deleteAllProducts();
    }
}