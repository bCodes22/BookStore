package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstoreapp.db.FirestoreHelper;
import com.example.bookstoreapp.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BooksActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView rvBooks;
    private TextView tvBookCount;
    private FirestoreHelper dbHelper;
    private FirebaseAuth mAuth;
    private android.widget.EditText etSearch;
    private List<Book> fullCatalogList = new ArrayList<>(); // Our backup master list
    private BookAdapter adapter;
    private List<Book> bookList;

    // Class variables clean up
    private String userId;
    private java.util.Set<String> userOwnedIds = new java.util.HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 2. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new FirestoreHelper();

        // Safety check: ensure user is actually logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 3. Save the UID to the class variable (no "String" prefix)
        userId = currentUser.getUid();

        // 4. Setup Views
        rvBooks     = findViewById(R.id.rvBooks);
        tvBookCount = findViewById(R.id.tvBookCount);

        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));

        // 5. Initialize an empty adapter immediately so the screen doesn't stay blank
        bookList = new ArrayList<>();
        adapter = new BookAdapter(this, bookList, userId, new java.util.HashSet<>(), this);
        rvBooks.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                filterBooks(s.toString());
            }
        });
        // 6. Fetch the real data
    }
    @Override
    protected void onResume() {
        super.onResume();
        // This fires every time the screen becomes visible!
        if (userId != null && !userId.isEmpty()) {
            loadBooksFromFirestore();
        }
    }

    private void loadBooksFromFirestore() {
        tvBookCount.setText("Loading books...");

        dbHelper.getUserLibrary(userId, new FirestoreHelper.OnLibraryLoadedListener() {
            @Override
            public void onSuccess(List<Book> ownedBooks, java.util.Set<String> ownedBookIds) {
                userOwnedIds = ownedBookIds;

                dbHelper.getUserWishlistIds(userId, new FirestoreHelper.OnWishlistIdsLoadedListener() {
                    @Override
                    public void onSuccess(java.util.Set<String> wishlistIds) {

                        // X-RAY VISION: Print exactly how many wishlist items we found
                        android.util.Log.d("BookStoreDebug", "Wishlist items downloaded: " + wishlistIds.size());
                        android.util.Log.d("BookStoreDebug", "Exact Wishlist IDs: " + wishlistIds.toString());

                        dbHelper.getAllBooks(new FirestoreHelper.OnBooksLoadedListener() {
                            @Override
                            public void onSuccess(List<Book> books) {
                                bookList.clear();
                                fullCatalogList.clear(); // Clear the old backup

                                for (Book book : books) {
                                    if (wishlistIds.contains(book.getId())) {
                                        book.setInWishlist(true);
                                    }
                                }

                                bookList.addAll(books);
                                fullCatalogList.addAll(books); // Save the backup!

                                adapter = new BookAdapter(BooksActivity.this, bookList, userId, userOwnedIds, BooksActivity.this);
                                rvBooks.setAdapter(adapter);

                                tvBookCount.setText(books.size() + " books available");
                            }

                            @Override
                            public void onFailure(Exception e) { }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) { }
                });
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }
    private void filterBooks(String text) {
        List<Book> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();

        for (Book book : fullCatalogList) {
            // Null checks are important here just in case a field is missing in Firestore!
            boolean matchesTitle = book.getTitle() != null && book.getTitle().toLowerCase().contains(query);
            boolean matchesAuthor = book.getAuthor() != null && book.getAuthor().toLowerCase().contains(query);
            boolean matchesGenre = book.getGenre() != null && book.getGenre().toLowerCase().contains(query);
            boolean matchesIsbn = false;
            if (book.getIsbn() != null) {
                for (String singleIsbn : book.getIsbn()) {
                    if (singleIsbn.toLowerCase().contains(query)) {
                        matchesIsbn = true;
                        break; // Stop looking once we find a match!
                    }
                }
            }
            if (matchesTitle || matchesAuthor || matchesGenre || matchesIsbn) {
                filteredList.add(book);
            }
        }

        if (adapter != null) {
            adapter.setFilteredList(filteredList);
            tvBookCount.setText(filteredList.size() + " books found");
        }
    }
    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // 1. This method draws the menu onto the Toolbar
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top_bar, menu);
        return true;
    }

    // 2. This method listens for clicks on that menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}