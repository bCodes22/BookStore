package com.example.bookstoreapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookstoreapp.db.FirestoreHelper;
import com.example.bookstoreapp.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final String userId;
    private final FirestoreHelper dbHelper;
    private final OnBookClickListener clickListener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public BookAdapter(Context context, List<Book> bookList, String userId, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.userId = userId;
        this.dbHelper = new FirestoreHelper();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // 1. Load Text Data
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvPrice.setText(String.format("$%.2f", book.getPrice()));

        // 2. Load Cover Image with Glide (FIXED: getImageUrl())
        if (book.getimageUrl() != null && !book.getimageUrl().isEmpty()) {
            // Hide the fallback text if we have an image
            holder.tvCoverTitle.setVisibility(View.GONE);

            Glide.with(context)
                    .load(book.getimageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivCoverImage);
        } else {
            // Fallback to your old color frame logic if no image exists
            holder.tvCoverTitle.setVisibility(View.VISIBLE);
            holder.tvCoverTitle.setText(book.getTitle());
            try {
                holder.coverFrame.setBackgroundColor(Color.parseColor(book.getCoverColor()));
            } catch (Exception e) {
                holder.coverFrame.setBackgroundColor(Color.parseColor("#2C5F8A"));
            }
        }

        // 3. Set Initial Wishlist State
        boolean isWished = book.isInWishlist();
        holder.tvWishlistToggle.setText(isWished ? "♥" : "♡");
        holder.tvWishlistToggle.setTextColor(isWished ? Color.parseColor("#FF4444") : Color.WHITE);

        // 4. Handle Wishlist Clicks (Optimistic Update)
        holder.tvWishlistToggle.setOnClickListener(v -> {
            boolean currentlyWished = book.isInWishlist();

            // Flip the state instantly for the user
            book.setInWishlist(!currentlyWished);
            notifyItemChanged(position);

            if (currentlyWished) {
                // Background cloud delete
                dbHelper.removeFromWishlist(userId, book.getId(), new FirestoreHelper.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Rollback UI if network fails
                        book.setInWishlist(true);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Background cloud add
                dbHelper.addToWishlist(userId, book.getId(), new FirestoreHelper.OnActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Rollback UI if network fails
                        book.setInWishlist(false);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 5. Book Card Click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onBookClick(book);
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        View coverFrame;
        ImageView ivCoverImage;
        TextView tvCoverTitle;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvPrice;
        TextView tvWishlistToggle;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            coverFrame       = itemView.findViewById(R.id.coverFrame);
            ivCoverImage     = itemView.findViewById(R.id.ivCoverImage);
            tvCoverTitle     = itemView.findViewById(R.id.tvCoverTitle);
            tvTitle          = itemView.findViewById(R.id.tvTitle);
            tvAuthor         = itemView.findViewById(R.id.tvAuthor);
            tvPrice          = itemView.findViewById(R.id.tvPrice);
            tvWishlistToggle = itemView.findViewById(R.id.tvWishlistToggle);
        }
    }
}