package com.example.bookstoreapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private final List<Book> bookList;
    private final int userId;
    private final DatabaseHelper dbHelper;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private OnBookClickListener clickListener;

    public BookAdapter(Context context, List<Book> bookList, int userId, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.userId = userId;
        this.dbHelper = new DatabaseHelper(context);
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

        // Cover color
        try {
            holder.coverFrame.setBackgroundColor(Color.parseColor(book.getCoverColor()));
        } catch (Exception e) {
            holder.coverFrame.setBackgroundColor(Color.parseColor("#2C5F8A"));
        }

        // Cover title
        holder.tvCoverTitle.setText(book.getTitle());

        // Info
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvPrice.setText(String.format("$%.2f", book.getPrice()));

        // Wishlist state
        boolean wished = dbHelper.isInWishlist(userId, book.getId());
        holder.tvWishlistToggle.setText(wished ? "♥" : "♡");
        holder.tvWishlistToggle.setTextColor(wished ? Color.parseColor("#FF4444") : Color.WHITE);

        // Wishlist toggle click
        holder.tvWishlistToggle.setOnClickListener(v -> {
            boolean currentlyWished = dbHelper.isInWishlist(userId, book.getId());
            if (currentlyWished) {
                dbHelper.removeFromWishlist(userId, book.getId());
                holder.tvWishlistToggle.setText("♡");
                holder.tvWishlistToggle.setTextColor(Color.WHITE);
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addToWishlist(userId, book.getId());
                holder.tvWishlistToggle.setText("♥");
                holder.tvWishlistToggle.setTextColor(Color.parseColor("#FF4444"));
                Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        // Book card click
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
        TextView tvCoverTitle;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvPrice;
        TextView tvWishlistToggle;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            coverFrame       = itemView.findViewById(R.id.coverFrame);
            tvCoverTitle     = itemView.findViewById(R.id.tvCoverTitle);
            tvTitle          = itemView.findViewById(R.id.tvTitle);
            tvAuthor         = itemView.findViewById(R.id.tvAuthor);
            tvPrice          = itemView.findViewById(R.id.tvPrice);
            tvWishlistToggle = itemView.findViewById(R.id.tvWishlistToggle);
        }
    }
}