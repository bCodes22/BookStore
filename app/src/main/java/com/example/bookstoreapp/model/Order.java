package com.example.bookstoreapp.model;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Order {

    private String id;

    @PropertyName("UserId")
    private String userId;

    @PropertyName("Items")
    private List<Book> items; // Firestore is smart enough to save a whole list of custom objects!

    @PropertyName("TotalPrice")
    private double totalPrice;

    @PropertyName("OrderDate")
    private long orderDate; // We use a timestamp (long) because it is easy to sort in the database

    public Order() {} // Required empty constructor for Firestore

    public Order(String userId, List<Book> items, double totalPrice, long orderDate) {
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Book> getItems() { return items; }
    public void setItems(List<Book> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public long getOrderDate() { return orderDate; }
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }
}