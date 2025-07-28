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

    @GetMapping("/search")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})  // Allow both roles
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
}