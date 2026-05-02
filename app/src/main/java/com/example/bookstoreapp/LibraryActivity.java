package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView rvLibrary;
    private LinearLayout layoutEmptyLibrary;
    private TextView tvLibraryCount;

    private FirestoreHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library); // We will create this XML next

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Library");
        }

        dbHelper = new FirestoreHelper();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();

        rvLibrary = findViewById(R.id.rvLibrary);
        layoutEmptyLibrary = findViewById(R.id.layoutEmptyLibrary);
        tvLibraryCount = findViewById(R.id.tvLibraryCount);

        rvLibrary.setLayoutManager(new GridLayoutManager(this, 3));


    }
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            loadLibrary();
        }
    }
    private void loadLibrary() {
        dbHelper.getUserLibrary(currentUserId, new FirestoreHelper.OnLibraryLoadedListener() {
            @Override
            public void onSuccess(List<Book> ownedBooks, Set<String> ownedBookIds) {
                if (ownedBooks.isEmpty()) {
                    layoutEmptyLibrary.setVisibility(View.VISIBLE);
                    rvLibrary.setVisibility(View.GONE);
                    tvLibraryCount.setVisibility(View.GONE);
                } else {
                    layoutEmptyLibrary.setVisibility(View.GONE);
                    rvLibrary.setVisibility(View.VISIBLE);
                    tvLibraryCount.setVisibility(View.VISIBLE);
                    tvLibraryCount.setText(ownedBooks.size() + " books owned");

                    // We can reuse the BookAdapter! We just pass the ownedBookIds so they all say "OWNED"
                    BookAdapter adapter = new BookAdapter(LibraryActivity.this, ownedBooks, currentUserId, ownedBookIds, LibraryActivity.this);
                    rvLibrary.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LibraryActivity.this, "Failed to load library", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBookClick(Book book) {
        // Open the detail screen so they can "Read" it
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
}