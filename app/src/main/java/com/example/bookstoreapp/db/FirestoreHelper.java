package com.example.bookstoreapp.db;

import com.example.bookstoreapp.model.Book;
import com.example.bookstoreapp.model.User;
import com.example.bookstoreapp.model.Order;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class FirestoreHelper {

    private final FirebaseFirestore db;

    // Standardize collection names
    private static final String COLLECTION_USERS = "Users";
    private static final String COLLECTION_BOOKS = "Books";
    private static final String COLLECTION_WISHLIST = "Wishlist";

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // ── Callbacks (Interfaces) ─────────────────────────────────────────────
    // Because network calls take time, we use interfaces to pass data back to the UI

    public interface OnBooksLoadedListener {
        void onSuccess(List<Book> books);
        void onFailure(Exception e);
    }

    public interface OnUserLoadedListener {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface OnActionListener {
        void onSuccess();
        void onFailure(Exception e);
    }
    public interface OnLibraryLoadedListener {
        void onSuccess(List<Book> ownedBooks, java.util.Set<String> ownedBookIds);
        void onFailure(Exception e);
    }
    public interface OnOrdersLoadedListener {
        void onSuccess(List<com.example.bookstoreapp.model.Order> orders);
        void onFailure(Exception e);
    }
    // ── Books ──────────────────────────────────────────────────────────────

    public void getAllBooks(OnBooksLoadedListener listener) {
        db.collection(COLLECTION_BOOKS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> books = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Firestore can automatically map documents to your Java class
                        // provided your Book class has an empty constructor and standard getters/setters.
                        Book book = document.toObject(Book.class);
                        
                        // Manually attach the document ID if your model needs it
                        book.setId(document.getId()); 
                        books.add(book);
                    }
                    listener.onSuccess(books);
                })
                .addOnFailureListener(listener::onFailure);
    }

    // ── Wishlist ───────────────────────────────────────────────────────────
    // In NoSQL, a wishlist is often a sub-collection or just a document linking a UserID and BookID

    public void addToWishlist(String userId, String bookId, OnActionListener listener) {
        // Create a unique ID for this wishlist entry to prevent duplicates
        String wishListEntryId = userId + "_" + bookId;

        Map<String, Object> wishData = new HashMap<>();
        wishData.put("userId", userId);
        wishData.put("bookId", bookId);

        db.collection(COLLECTION_WISHLIST).document(wishListEntryId)
                .set(wishData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void removeFromWishlist(String userId, String bookId, OnActionListener listener) {
        String wishListEntryId = userId + "_" + bookId;

        db.collection(COLLECTION_WISHLIST).document(wishListEntryId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // ── Users ──────────────────────────────────────────────────────────────

    public void registerUser(User user, OnActionListener listener) {
        // Warning: For real production apps, you should use Firebase Authentication 
        // instead of storing passwords directly in Firestore, even if hashed.
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail().toLowerCase());
        userData.put("password", user.getPassword()); // Consider hashing before passing to this method
        userData.put("authType", user.getAuthType());
        userData.put("createdAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .add(userData)
                .addOnSuccessListener(documentReference -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void loginUser(String email, String password, OnUserLoadedListener listener) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.toLowerCase())
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure(new Exception("Invalid credentials"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }
    public void placeOrder(Order order, OnActionListener listener) {
        // We let Firestore auto-generate a random receipt ID by calling .document() empty
        db.collection("Orders").document()
                .set(order)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void listenForBookUpdates() {
        db.collection("Books")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("FirestoreHelper", "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null) {
                        // Loop through ONLY the documents that have changed
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Book changedBook = dc.getDocument().toObject(Book.class);
                            changedBook.setId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    // A new book was added (or it's the first time loading the cache)
                                    Log.d("Firestore", "New book: " + changedBook.getTitle());
                                    // TODO: Add to your local ArrayList and notify the adapter
                                    break;

                                case MODIFIED:
                                    // A book's price, stock, or data changed
                                    Log.d("Firestore", "Modified book: " + changedBook.getTitle());
                                    // TODO: Find this book in your list by ID and update it
                                    break;

                                case REMOVED:
                                    // An admin deleted this book
                                    Log.d("Firestore", "Removed book: " + changedBook.getTitle());
                                    // TODO: Remove from your ArrayList and notify the adapter
                                    break;
                            }
                        }
                    }
                });
    }
    // Add this method to fetch the owned books
    public void getUserLibrary(String userId, OnLibraryLoadedListener listener) {
        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    // Use a Map to prevent duplicates if they bought the same book twice
                    java.util.Map<String, Book> uniqueBooks = new java.util.HashMap<>();
                    java.util.Set<String> ownedIds = new java.util.HashSet<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        com.example.bookstoreapp.model.Order order = doc.toObject(com.example.bookstoreapp.model.Order.class);
                        if (order != null && order.getItems() != null) {
                            for (Book book : order.getItems()) {
                                uniqueBooks.put(book.getId(), book);
                                ownedIds.add(book.getId());
                            }
                        }
                    }
                    // Return both the full books (for the Library) and just the IDs (for the Store UI)
                    listener.onSuccess(new java.util.ArrayList<>(uniqueBooks.values()), ownedIds);
                })
                .addOnFailureListener(listener::onFailure);
    }
    public void getUserOrders(String userId, OnOrdersLoadedListener listener) {
        db.collection("Orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<com.example.bookstoreapp.model.Order> orders = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                        com.example.bookstoreapp.model.Order order = doc.toObject(com.example.bookstoreapp.model.Order.class);
                        if (order != null) {
                            order.setId(doc.getId());
                            orders.add(order);
                        }
                    }

                    // Sort newest to oldest
                    java.util.Collections.sort(orders, (o1, o2) -> Long.compare(o2.getOrderDate(), o1.getOrderDate()));

                    listener.onSuccess(orders);
                })
                .addOnFailureListener(listener::onFailure);
    }
}