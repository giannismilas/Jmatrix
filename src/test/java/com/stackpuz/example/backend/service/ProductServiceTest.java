package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setPrice(100.0);
    }

    @Test
    void saveProduct_ShouldReturnSavedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.saveProduct(product);

        assertNotNull(saved);
        assertEquals("Test Product", saved.getName());
        verify(productRepository).save(product);
    }

    @Test
    void getProducts_ShouldReturnAllProducts() {
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getProducts();

        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenExists() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(1);

        assertEquals("Test Product", found.getName());
        verify(productRepository).findById(1);
    }

    @Test
    void getProductById_ShouldThrowException_WhenNotFound() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.getProductById(999));
    }

    @Test
    void updateProduct_ShouldUpdateAndReturnProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(150.0);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1, updatedProduct);

        assertEquals("Updated Product", result.getName());
        verify(productRepository).findById(1);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldThrowException_WhenNotFound() {
        Product updatedProduct = new Product();
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                productService.updateProduct(999, updatedProduct));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenExists() {
        when(productRepository.existsById(1)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1);

        productService.deleteProduct(1);

        verify(productRepository).existsById(1);
        verify(productRepository).deleteById(1);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenNotFound() {
        when(productRepository.existsById(999)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> productService.deleteProduct(999));
    }
}