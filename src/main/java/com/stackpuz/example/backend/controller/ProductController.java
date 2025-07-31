package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})  // Allow both ADMIN and VIEWER roles
    public String getProducts(Model model) {
        List<Product> products = service.getProducts();
        model.addAttribute("products", products);
        return "products";
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
    public void deleteProduct(@PathVariable int id) {
        service.deleteProduct(id);
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