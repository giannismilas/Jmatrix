package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.CartItem;
import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.CartRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Objects;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addToCart(User user, Integer productId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.getProductById(productId);
        
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> Objects.equals(item.getProduct().getId(), productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(User user, Integer productId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> Objects.equals(item.getProduct().getId(), productId));
        cartRepository.save(cart);
    }

    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }
}