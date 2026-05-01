package com.example.bookstoreapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bookstoreapp.db.FirestoreHelper;
import com.example.bookstoreapp.model.Book;
import com.example.bookstoreapp.model.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookDetailActivity extends AppCompatActivity {

    // UI Elements
    private ImageView ivCover;
    private TextView tvTitle, tvAuthor, tvPrice, tvDescription;
    private Button btnAddToCart, btnAddToWishlist;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private FirestoreHelper dbHelper;
    private String currentUserId;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // We will show the title in the UI instead
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        dbHelper = new FirestoreHelper();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();

        // Catch the ID sent from BooksActivity
        bookId = getIntent().getStringExtra("BOOK_ID");

        if (bookId != null) {
            fetchBookDetails(bookId);
        } else {
            Toast.makeText(this, "Error loading book.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ivCover = findViewById(R.id.ivDetailCover);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToWishlist = findViewById(R.id.btnAddToWishlist);
        progressBar = findViewById(R.id.progressBar);

        // Hide UI until data loads
        setUiVisibility(View.GONE);
    }

    private void fetchBookDetails(String id) {
        db.collection("Books").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book book = documentSnapshot.toObject(Book.class);
                        if (book != null) {
                            book.setId(documentSnapshot.getId());
                            populateUI(book);
                        }
                    } else {
                        Toast.makeText(this, "Book not found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetail", "Error fetching book", e);
                    Toast.makeText(this, "Failed to load details.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateUI(Book book) {
        // Load Text
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvPrice.setText(String.format("$%.2f", book.getPrice()));

        // Handle potentially missing descriptions gracefully
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            tvDescription.setText(book.getDescription());
        } else {
            tvDescription.setText("No description available for this book.");
        }

        // Load Cover Image
        if (book.getimageUrl() != null && !book.getimageUrl().isEmpty()) {
            Glide.with(this)
                    .load(book.getimageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivCover);
        }

        // Setup Buttons
        btnAddToWishlist.setOnClickListener(v -> {
            if (currentUserId != null) {
                dbHelper.addToWishlist(currentUserId, book.getId(), new FirestoreHelper.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(BookDetailActivity.this, "Added to Wishlist!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(BookDetailActivity.this, "Failed to add to Wishlist", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            // Add the current book to our local cart
            CartManager.getInstance().addToCart(book);
            Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
        });
        // Show UI, Hide Spinner
        setUiVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void setUiVisibility(int visibility) {
        ivCover.setVisibility(visibility);
        tvTitle.setVisibility(visibility);
        tvAuthor.setVisibility(visibility);
        tvPrice.setVisibility(visibility);
        tvDescription.setVisibility(visibility);
        btnAddToCart.setVisibility(visibility);
        btnAddToWishlist.setVisibility(visibility);
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
            // User clicked the cart icon!
            android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
            startActivity(intent);
            // Optional: add your slide animations here
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }

        // This handles the default back arrow if it exists
        return super.onOptionsItemSelected(item);
    }
}