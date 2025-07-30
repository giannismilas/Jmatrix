package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.repository.OrderRepository;
import com.stackpuz.example.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductService service;

    @Autowired
    private OrderRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll(); // ΠΡΩΤΑ τα εξαρτώμενα (children)
        productRepository.deleteAll();   // ΜΕΤΑ τα εξαρτώμενα (parent)
    }

    @Test
    void saveProduct_ShouldReturnSavedProduct() {
        Product product = new Product();
        product.setName("PS5");
        product.setPrice(399.99);

        Product saved = service.saveProduct(product);

        assertNotNull(saved.getId());
        assertEquals("PS5", saved.getName());
        assertEquals(399.99, saved.getPrice());
    }

    @Test
    void getProducts_ShouldReturnAllProducts() {
        Product p1 = new Product();
        p1.setName("PS5");
        p1.setPrice(399.99);

        Product p2 = new Product();
        p2.setName("Xbox");
        p2.setPrice(299.99);

        service.saveProduct(p1);
        service.saveProduct(p2);

        List<Product> products = service.getProducts();
        assertEquals(2, products.size());
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenExists() {
        Product product = new Product();
        product.setName("Nintendo Switch");
        product.setPrice(199.99);

        Product saved = service.saveProduct(product);

        Product found = service.getProductById(saved.getId());
        assertEquals("Nintendo Switch", found.getName());
    }

    @Test
    void getProductById_ShouldThrowException_WhenNotFound() {
        assertThrows(EntityNotFoundException.class, () -> service.getProductById(999));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() {
        Product product = new Product();
        product.setName("Tablet");
        product.setPrice(150.0);
        Product saved = service.saveProduct(product);

        Product updated = new Product();
        updated.setName("Updated Tablet");
        updated.setPrice(170.0);

        Product result = service.updateProduct(saved.getId(), updated);

        assertEquals("Updated Tablet", result.getName());
        assertEquals(170.0, result.getPrice());
    }

    @Test
    void updateProduct_ShouldThrowException_WhenNotFound() {
        Product updated = new Product();
        updated.setName("Fake");
        updated.setPrice(0.0);

        assertThrows(EntityNotFoundException.class, () -> service.updateProduct(999, updated));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenExists() {
        Product product = new Product();
        product.setName("Camera");
        product.setPrice(299.99);
        Product saved = service.saveProduct(product);

        service.deleteProduct(saved.getId());

        assertFalse(repository.existsById(saved.getId()));
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenNotFound() {
        assertThrows(EntityNotFoundException.class, () -> service.deleteProduct(999));
    }
}
