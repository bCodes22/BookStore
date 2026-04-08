package com.example.bookstoreapp.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String authType; // "email" or "google"
    private long createdAt;

    public User() {}

    public User(String name, String email, String password, String authType) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.authType = authType;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}