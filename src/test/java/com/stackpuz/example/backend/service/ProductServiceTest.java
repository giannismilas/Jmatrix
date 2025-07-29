package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class ProductServiceTest {


    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void saveProduct_ShouldReturnSavedProduct() {

        Product savedProduct = new Product();
        savedProduct.setName("PS5");
        savedProduct.setPrice(399.99);

        repository.save(savedProduct);

      Product product = service.saveProduct(savedProduct);

      Assertions.assertEquals("PS5", product.getName());
      Assertions.assertEquals(savedProduct.getPrice(), product.getPrice());



    }

}