package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
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

    @GetMapping("/search")
    public String searchProductById(@RequestParam(required = false) Integer id, Model model) {
        if (id == null) {
            return "redirect:/products";
        }
        
        try {
            Product product = service.getProductById(id);
            model.addAttribute("products", List.of(product));
            model.addAttribute("searchId", id);
            return "products";
        } catch (EntityNotFoundException e) {
            model.addAttribute("products", List.of());
            model.addAttribute("searchId", id);
            return "products";
        }
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


}