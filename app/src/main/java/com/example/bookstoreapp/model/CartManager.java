package com.example.bookstoreapp.model;

import com.example.bookstoreapp.model.Book;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<Book> cartItems;

    // Private constructor so no one else can make a new cart
    private CartManager() {
        cartItems = new ArrayList<>();
    }

    // Get the single shared cart
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Book book) {
        cartItems.add(book);
    }

    public List<Book> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Book book : cartItems) {
            total += book.getPrice();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}