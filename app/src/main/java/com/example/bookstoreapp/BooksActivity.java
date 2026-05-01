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

    // We use FirebaseAuth now instead of SessionManager
    private FirebaseAuth mAuth;

    // Keep a reference to the adapter and list so we can update them when data arrives
    private BookAdapter adapter;
    private List<Book> bookList;

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
        dbHelper = new FirestoreHelper(); // No longer needs 'this' context

        // Safety check: ensure user is actually logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get the secure string UID from Firebase Auth
        String userId = currentUser.getUid();

        // 3. Setup Views
        rvBooks     = findViewById(R.id.rvBooks);
        tvBookCount = findViewById(R.id.tvBookCount);

        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));

        // 4. Initialize an empty adapter immediately so the screen doesn't stay blank
        bookList = new ArrayList<>();
        adapter = new BookAdapter(this, bookList, userId, this);
        rvBooks.setAdapter(adapter);

        // 5. Fetch the real data
        loadBooksFromFirestore();
    }

    private void loadBooksFromFirestore() {
        // Show a temporary loading message
        tvBookCount.setText("Loading books...");

        // Use the callback interface we built in FirestoreHelper
        dbHelper.getAllBooks(new FirestoreHelper.OnBooksLoadedListener() {
            @Override
            public void onSuccess(List<Book> books) {
                // Clear the empty list and add all the fresh books from the cloud
                bookList.clear();
                bookList.addAll(books);

                // Tell the adapter to redraw the screen
                adapter.notifyDataSetChanged();

                // Update the count UI
                tvBookCount.setText(books.size() + " books available");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("BooksActivity", "Error loading books", e);
                Toast.makeText(BooksActivity.this, "Failed to load books. Check your connection.", Toast.LENGTH_LONG).show();
                tvBookCount.setText("0 books available");
            }
        });
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(this, BookDetailActivity.class);

        // Pass the Firestore document ID to the next screen
        // We will use this ID to fetch the full book details from the database
        intent.putExtra("BOOK_ID", book.getId());

        startActivity(intent);

        // Keep your smooth transitions!
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
}