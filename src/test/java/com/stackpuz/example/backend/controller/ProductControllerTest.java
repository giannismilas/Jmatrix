package com.stackpuz.example.backend.controller;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1);
        product1.setName("Laptop");
        product1.setPrice(999.99);

        product2 = new Product();
        product2.setId(2);
        product2.setName("Phone");
        product2.setPrice(699.99);
    }

    @Test
    void getProducts_ShouldReturnProductsViewWithAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getProducts()).thenReturn(products);

        // Act
        String viewName = productController.getProducts(model);

        // Assert
        assertEquals("products", viewName);
        verify(model).addAttribute("products", products);
        verify(productService).getProducts();
    }

    @Test
    void searchProducts_WithNumericQuery_ShouldSearchById() {
        // Arrange
        String query = "1";
        when(productService.getProductById(1)).thenReturn(product1);

        // Act
        String viewName = productController.searchProducts(query, model);

        // Assert
        assertEquals("products", viewName);
        verify(productService).getProductById(1);
        verify(productService, never()).searchProductsByName(anyString());
        verify(model).addAttribute("products", Collections.singletonList(product1));
        verify(model).addAttribute("searchId", 1);
        verify(model, never()).addAttribute(eq("searchName"), anyString());
    }

    @Test
    void searchProducts_WithNumericQueryButInvalidId_ShouldSearchByName() {
        // Arrange
        String query = "999";
        when(productService.getProductById(999)).thenThrow(new EntityNotFoundException());
        when(productService.searchProductsByName(query)).thenReturn(Collections.emptyList());

        // Act
        String viewName = productController.searchProducts(query, model);

        // Assert
        assertEquals("products", viewName);
        verify(productService).getProductById(999);
        verify(productService).searchProductsByName(query);
        verify(model).addAttribute("products", Collections.emptyList());
        verify(model).addAttribute("searchName", query);
        verify(model, never()).addAttribute(eq("searchId"), anyInt());
    }

    @Test
    void searchProducts_WithTextQuery_ShouldSearchByName() {
        // Arrange
        String query = "lap";
        when(productService.searchProductsByName(query)).thenReturn(Collections.singletonList(product1));

        // Act
        String viewName = productController.searchProducts(query, model);

        // Assert
        assertEquals("products", viewName);
        verify(productService, never()).getProductById(anyInt());
        verify(productService).searchProductsByName(query);
        verify(model).addAttribute("products", Collections.singletonList(product1));
        verify(model).addAttribute("searchName", query);
        verify(model, never()).addAttribute(eq("searchId"), anyInt());
    }

    @Test
    void searchProducts_WithEmptyQuery_ShouldRedirectToProducts() {
        // Act
        String viewName = productController.searchProducts("", model);

        // Assert
        assertEquals("redirect:/products", viewName);
        verifyNoInteractions(productService);
    }

    @Test
    void searchProducts_WithNullQuery_ShouldRedirectToProducts() {
        // Act
        String viewName = productController.searchProducts(null, model);

        // Assert
        assertEquals("redirect:/products", viewName);
        verifyNoInteractions(productService);
    }

    @Test
    void searchProducts_WithNonNumericQuery_ShouldSearchByName() {
        // Arrange
        String query = "laptop";
        when(productService.searchProductsByName(query)).thenReturn(Collections.singletonList(product1));

        // Act
        String viewName = productController.searchProducts(query, model);

        // Assert
        assertEquals("products", viewName);
        verify(productService, never()).getProductById(anyInt());
        verify(productService).searchProductsByName(query);
        verify(model).addAttribute("products", Collections.singletonList(product1));
        verify(model).addAttribute("searchName", query);
    }

    @Test
    void createProduct_ShouldReturnSavedProduct() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Tablet");
        newProduct.setPrice(299.99);

        when(productService.saveProduct(newProduct)).thenReturn(product1);

        // Act
        Product result = productController.createProduct(newProduct);

        // Assert
        assertEquals(product1, result);
        verify(productService).saveProduct(newProduct);
    }

    @Test
    void updateProduct_WithValidId_ShouldReturnUpdatedProduct() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setName("Laptop Pro");
        updatedProduct.setPrice(1099.99);

        when(productService.updateProduct(1, updatedProduct)).thenReturn(product1);

        // Act
        Product result = productController.updateProduct(1, updatedProduct);

        // Assert
        assertEquals(product1, result);
        verify(productService).updateProduct(1, updatedProduct);
    }

    @Test
    void deleteProduct_WithValidId_ShouldCallDeleteService() {
        // Act
        productController.deleteProduct(1);

        // Assert
        verify(productService).deleteProduct(1);
    }
}