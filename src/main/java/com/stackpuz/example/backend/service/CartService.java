package com.stackpuz.example.backend.service;

import com.stackpuz.example.backend.entity.Cart;
import com.stackpuz.example.backend.entity.CartItem;
import com.stackpuz.example.backend.entity.Product;
import com.stackpuz.example.backend.entity.User;
import com.stackpuz.example.backend.repository.CartRepository;
import com.stackpuz.example.backend.repository.ProductRepository;
import com.stackpuz.example.backend.service.DiscountCodeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DiscountCodeService discountCodeService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, DiscountCodeService discountCodeService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.discountCodeService = discountCodeService;
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
        // Also clear any applied discount so it does not persist across orders
        cart.setAppliedDiscountCode(null);
        cart.setAppliedDiscountPercent(null);
        cartRepository.save(cart);
    }

    public Cart applyDiscountCode(User user, String code) {
        Cart cart = getUserCart(user);
        var dc = discountCodeService.requireValidActive(code);
        cart.setAppliedDiscountCode(dc.getCode());
        cart.setAppliedDiscountPercent(dc.getPercent());
        return cartRepository.save(cart);
    }

    public Cart clearDiscount(User user) {
        Cart cart = getUserCart(user);
        cart.setAppliedDiscountCode(null);
        cart.setAppliedDiscountPercent(null);
        return cartRepository.save(cart);
    }
}