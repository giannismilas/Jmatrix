package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.CartItem;
import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.CartRepository;
import com.stackpuz.example.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        cartService = new CartService(cartRepository, productRepository);

        user = new User();

        product = new Product();
        product.setId(1);
        product.setPrice(10.0);
    }

    @Test
    void testGetUserCart_existingCart() {
        Cart existingCart = new Cart();
        existingCart.setUser(user);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(existingCart));

        Cart cart = cartService.getUserCart(user);

        assertEquals(existingCart, cart);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testGetUserCart_createNewCart() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Cart cart = cartService.getUserCart(user);

        assertNotNull(cart);
        assertEquals(user, cart.getUser());
        verify(cartRepository).save(cart);
    }

    @Test
    void testAddToCart_addNewItem() {
        Cart cart = new Cart();
        cart.setUser(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.addToCart(user, product.getId(), 2);

        assertEquals(1, cart.getItems().size());
        CartItem item = cart.getItems().get(0);
        assertEquals(product, item.getProduct());
        assertEquals(2, item.getQuantity());
        assertEquals(cart, item.getCart());

        verify(cartRepository).save(cart);
    }

    @Test
    void testAddToCart_updateExistingItemQuantity() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.addToCart(user, product.getId(), 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(4, item.getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void testAddToCart_productNotFound() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart()));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            cartService.addToCart(user, product.getId(), 1);
        });
    }

    @Test
    void testUpdateCartItemQuantity_updateQuantity() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.updateCartItemQuantity(user, product.getId(), 5);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, item.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void testUpdateCartItemQuantity_removeItemWhenZeroOrLess() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.updateCartItemQuantity(user, product.getId(), 0);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void testRemoveFromCart() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item1 = new CartItem();
        item1.setProduct(product);
        item1.setQuantity(1);
        item1.setCart(cart);

        Product product2 = new Product();
        product2.setId(2);

        CartItem item2 = new CartItem();
        item2.setProduct(product2);
        item2.setQuantity(3);
        item2.setCart(cart);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.removeFromCart(user, product.getId());

        assertEquals(1, cart.getItems().size());
        assertEquals(product2, cart.getItems().get(0).getProduct());
        verify(cartRepository).save(cart);
    }

    @Test
    void testClearCart() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cartService.clearCart(user);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }
}
