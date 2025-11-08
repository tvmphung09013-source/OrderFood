package com.example.orderfood.service;

import android.util.Log;

import com.example.orderfood.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static final String TAG = "CartService";
    private static final List<Product> cart = new ArrayList<>();

    public void addToCart(Product product) {
        cart.add(product);
        Log.d(TAG, product.getName() + " added to cart.");
    }

    public List<Product> getCartItems() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
    }
}
