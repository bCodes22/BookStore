package com.example.bookstoreapp.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String coverUrl;      // image link
    private String coverColor;    // fallback color for placeholder UI
    private double price;
    private String description;
    private boolean inWishlist;

    public Book() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCoverColor() { return coverColor; }
    public void setCoverColor(String coverColor) { this.coverColor = coverColor; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isInWishlist() { return inWishlist; }
    public void setInWishlist(boolean inWishlist) { this.inWishlist = inWishlist; }
}