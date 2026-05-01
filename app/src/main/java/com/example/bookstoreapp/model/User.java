package com.example.bookstoreapp.model;

public class User {

    // Change this from int to String
    private String id;

    private String name;
    private String email;
    private String password;
    private String authType;
    private long createdAt;

    public User() {
        // Empty constructor needed for Firestore
    }
    // Getters and Setters
// Update your getter
    public String getId() {
        return id;
    }

    // Update your setter
    public void setId(String id) {
        this.id = id;
    }
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