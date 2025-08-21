package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.repository.ProductRepository;
import com.stackpuz.example.backend.repository.ReviewRepository;
import com.stackpuz.example.backend.repository.CartItemRepository;
import com.stackpuz.example.backend.repository.WishlistItemRepository;
import com.stackpuz.example.backend.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ReviewRepository reviewRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final OrderItemRepository orderItemRepository;

    public Product saveProduct(Product product) {
        return repository.save(product);
    }

    public List<Product> getProducts() {
        return repository.findAll();
    }

    public Product getProductById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public Product updateProduct(int id, Product product) {
        Product existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return repository.save(existing);
    }

    @Transactional
    public void deleteProduct(int id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        // Do not delete products that appear in orders (preserve order history)
        long orderRefs = orderItemRepository.countByProductId(id);
        if (orderRefs > 0) {
            throw new IllegalStateException("Cannot delete product because it exists in one or more orders");
        }
        // Remove product from all carts and wishlists
        cartItemRepository.deleteByProductId(id);
        wishlistItemRepository.deleteByProductId(id);
        // First delete dependent reviews to satisfy FK constraints
        reviewRepository.deleteByProductId(id);
        repository.deleteById(id);
    }

    public List<Product> searchProductsByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

}