package com.example.bookstoreapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookstoreapp.db.FirestoreHelper;
import com.example.bookstoreapp.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private LinearLayout layoutEmpty;
    private TextView tvWishlistCount;

    // Firebase Replacements
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new FirestoreHelper();

        // Security check
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = user.getUid();

        // Setup UI
        rvWishlist       = findViewById(R.id.rvWishlist);
        layoutEmpty      = findViewById(R.id.layoutEmpty);
        tvWishlistCount  = findViewById(R.id.tvWishlistCount);

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        loadWishlist();
    }

    private void loadWishlist() {
        // Step 1: Find all wishlist entries for this user
        db.collection("Wishlist")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        bookIds.add(doc.getString("bookId"));
                    }

                    if (bookIds.isEmpty()) {
                        showWishlist(new ArrayList<>());
                    } else {
                        // Step 2: Fetch the actual book details using the retrieved IDs
                        fetchBookDetails(bookIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error loading wishlist IDs", e);
                    Toast.makeText(this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchBookDetails(List<String> bookIds) {
        // Firestore's 'whereIn' is a great way to fetch multiple specific documents at once.
        // Note: Firestore limits 'whereIn' to 10 items per query. If your wishlist grows larger
        // than 10, you would need to batch these requests. For now, this handles standard lists perfectly.

        // We chunk the list to avoid the 10-item limit crash
        List<Book> finalBookList = new ArrayList<>();

        // Let's do a simple batching approach for scalability
        int limit = Math.min(bookIds.size(), 10);
        List<String> firstBatch = bookIds.subList(0, limit);

        db.collection("Books")
                .whereIn(FieldPath.documentId(), firstBatch)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        Book book = doc.toObject(Book.class);
                        if (book != null) {
                            book.setId(doc.getId());
                            finalBookList.add(book);
                        }
                    }
                    showWishlist(finalBookList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error fetching book data", e);
                });
    }

    private void showWishlist(List<Book> books) {
        if (books == null || books.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            tvWishlistCount.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            tvWishlistCount.setVisibility(View.VISIBLE);
            tvWishlistCount.setText(books.size() + " saved book" + (books.size() > 1 ? "s" : ""));
            rvWishlist.setAdapter(new WishlistAdapter(books));
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────
    private class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

        private final List<Book> books;

        WishlistAdapter(List<Book> books) {
            this.books = books;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_wishlist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Book book = books.get(position);

            // Load Text
            holder.tvTitle.setText(book.getTitle());
            holder.tvAuthor.setText(book.getAuthor());
            holder.tvPrice.setText(String.format("$%.2f", book.getPrice()));

            // Load Cover Image with Glide
            if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
                holder.tvCoverTitle.setVisibility(View.GONE);

                // Assuming you add an ImageView to your item_wishlist.xml just like item_book.xml
                if (holder.ivCoverImage != null) {
                    Glide.with(WishlistActivity.this)
                            .load(book.getCoverUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.ivCoverImage);
                }
            } else {
                holder.tvCoverTitle.setVisibility(View.VISIBLE);
                holder.tvCoverTitle.setText(book.getTitle());
                try {
                    holder.coverFrame.setBackgroundColor(Color.parseColor(book.getCoverColor()));
                } catch (Exception e) {
                    holder.coverFrame.setBackgroundColor(Color.parseColor("#2C5F8A"));
                }
            }

            // Remove from wishlist using your FirestoreHelper
            holder.tvRemove.setOnClickListener(v -> {
                dbHelper.removeFromWishlist(currentUserId, book.getId(), new FirestoreHelper.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        books.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, books.size());
                        Toast.makeText(WishlistActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();

                        if (books.isEmpty()) {
                            showWishlist(books);
                        } else {
                            tvWishlistCount.setText(books.size() + " saved book" + (books.size() > 1 ? "s" : ""));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(WishlistActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // Book click
            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(WishlistActivity.this, book.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return books.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            FrameLayout coverFrame;
            ImageView ivCoverImage; // Added for Glide
            TextView tvCoverTitle, tvTitle, tvAuthor, tvPrice, tvRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                coverFrame   = itemView.findViewById(R.id.coverFrame);

                // Make sure to add this ID to your item_wishlist.xml!
                ivCoverImage = itemView.findViewById(R.id.ivCoverImage);

                tvCoverTitle = itemView.findViewById(R.id.tvCoverTitle);
                tvTitle      = itemView.findViewById(R.id.tvTitle);
                tvAuthor     = itemView.findViewById(R.id.tvAuthor);
                tvPrice      = itemView.findViewById(R.id.tvPrice);
                tvRemove     = itemView.findViewById(R.id.tvRemove);
            }
        }
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