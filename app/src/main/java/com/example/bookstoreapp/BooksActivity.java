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

        // 1. Fetch the Owned IDs first
        dbHelper.getUserLibrary(userId, new FirestoreHelper.OnLibraryLoadedListener() {
            @Override
            public void onSuccess(List<Book> ownedBooks, java.util.Set<String> ownedBookIds) {
                userOwnedIds = ownedBookIds; // Save them

                // 2. NOW fetch the main catalog
                dbHelper.getAllBooks(new FirestoreHelper.OnBooksLoadedListener() {
                    @Override
                    public void onSuccess(List<Book> books) {
                        bookList.clear();
                        bookList.addAll(books);

                        // Recreate the adapter with the new owned IDs
                        adapter = new BookAdapter(BooksActivity.this, bookList, userId, userOwnedIds, BooksActivity.this);
                        rvBooks.setAdapter(adapter);

                        tvBookCount.setText(books.size() + " books available");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(BooksActivity.this, "Failed to load catalog.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(BooksActivity.this, "Failed to load user library.", Toast.LENGTH_SHORT).show();
            }
        });
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