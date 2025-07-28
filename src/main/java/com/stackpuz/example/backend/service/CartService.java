package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.CartItem;
import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.CartRepository;
import com.stackpuz.example.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public void addToCart(User user, Integer productId, int quantity) {
        Cart cart = getUserCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
                
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == product.getId())
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProduct(product);
                            newItem.setQuantity(quantity);
                            cart.getItems().add(newItem);
                        }
                );
        cartRepository.save(cart);
    }

    public void updateCartItemQuantity(User user, Integer productId, int quantity) {
        Cart cart = getUserCart(user);
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        cartRepository.save(cart);
    }

    public void removeFromCart(User user, Integer productId) {
        Cart cart = getUserCart(user);
        cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        cartRepository.save(cart);
    }

    public void clearCart(User user) {
        Cart cart = getUserCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}