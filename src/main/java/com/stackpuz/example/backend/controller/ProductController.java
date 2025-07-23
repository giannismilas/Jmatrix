package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/api/products")
    public List<Product> getProducts() {
        return service.getProducts();
    }

    @GetMapping("/api/products/{id}")
    public Product getProduct(@PathVariable int id) {
        return service.getProductById(id);
    }

    @PostMapping("/api/products")
    public Product createProduct(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    @PutMapping("/api/products/{id}")
    public Product updateProduct(@PathVariable int id, @RequestBody Product product) {
        return service.updateProduct(id, product);
    }

    @DeleteMapping("/api/products/{id}")
    public void deleteProduct(@PathVariable int id) {
        service.deleteProduct(id);
    }

    @DeleteMapping("/api/products")
    public void deleteAllProducts() {
        service.deleteAllProducts();
    }
}