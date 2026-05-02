package com.example.bookstoreapp.model;

public class Book {

    // Changed from int to String to handle Firestore Document IDs
    private String id;

    private String title;
    private String author;
    private String imageUrl;      // image link
    private String coverColor;    // fallback color for placeholder UI
    private double price;
    private String description;
    private boolean inWishlist;
    private String genre;
    private java.util.List<String> ISBN;

    public Book() {}

    // Updated Getter
    public String getId() { return id; }

    // Updated Setter
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getimageUrl() { return imageUrl; }
    public void setimageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCoverColor() { return coverColor; }
    public void setCoverColor(String coverColor) { this.coverColor = coverColor; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isInWishlist() { return inWishlist; }
    public void setInWishlist(boolean inWishlist) { this.inWishlist = inWishlist; }
    public String getGenre() { return genre; }
    public void setGenre(String title) { this.genre = title; }
    // Getter
    public java.util.List<String> getIsbn() {
        return ISBN;
    }

    // Setter
    public void setIsbn(java.util.List<String> isbn) {
        this.ISBN = ISBN;
    }
}