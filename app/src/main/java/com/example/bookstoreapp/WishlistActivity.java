package com.example.bookstoreapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.db.SessionManager;
import com.example.bookstoreapp.model.Book;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private LinearLayout layoutEmpty;
    private TextView tvWishlistCount;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper       = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvWishlist       = findViewById(R.id.rvWishlist);
        layoutEmpty      = findViewById(R.id.layoutEmpty);
        tvWishlistCount  = findViewById(R.id.tvWishlistCount);

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        loadWishlist();
    }

    private void loadWishlist() {
        // ── TODO (Firebase): Replace this with a Firebase query like:
        //    db.collection("wishlists")
        //      .whereEqualTo("userId", sessionManager.getUserId())
        //      .get()
        //      .addOnSuccessListener(snapshot -> {
        //          List<Book> books = snapshot.toObjects(Book.class);
        //          showWishlist(books);
        //      });
        // ────────────────────────────────────────────────────────────

        int userId = sessionManager.getUserId();
        List<Book> wishlistBooks = dbHelper.getWishlistBooks(userId);
        showWishlist(wishlistBooks);
    }

    private void showWishlist(List<Book> books) {
        if (books == null || books.isEmpty()) {
            // Show empty state
            layoutEmpty.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
            tvWishlistCount.setVisibility(View.GONE);
        } else {
            // Show list
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

            // Cover color
            try {
                holder.coverFrame.setBackgroundColor(Color.parseColor(book.getCoverColor()));
            } catch (Exception e) {
                holder.coverFrame.setBackgroundColor(Color.parseColor("#2C5F8A"));
            }

            holder.tvCoverTitle.setText(book.getTitle());
            holder.tvTitle.setText(book.getTitle());
            holder.tvAuthor.setText(book.getAuthor());
            holder.tvPrice.setText(String.format("$%.2f", book.getPrice()));

            // Remove from wishlist when linked to server
            holder.tvRemove.setOnClickListener(v -> {
                // ── TODO (Firebase): Replace with Firebase delete:
                //    db.collection("wishlists")
                //      .document(userId + "_" + book.getId())
                //      .delete();
                // ────────────────────────────────────────────────

                int userId = sessionManager.getUserId();
                dbHelper.removeFromWishlist(userId, book.getId());
                books.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, books.size());
                Toast.makeText(WishlistActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();

                // If list is now empty, show empty state
                if (books.isEmpty()) {
                    showWishlist(books);
                } else {
                    tvWishlistCount.setText(books.size() + " saved book" + (books.size() > 1 ? "s" : ""));
                }
            });

            // Book click
            holder.itemView.setOnClickListener(v -> {
                // TODO: Open BookDetailActivity
                Toast.makeText(WishlistActivity.this, book.getTitle(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return books.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            FrameLayout coverFrame;
            TextView tvCoverTitle, tvTitle, tvAuthor, tvPrice, tvRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                coverFrame   = itemView.findViewById(R.id.coverFrame);
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