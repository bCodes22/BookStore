package com.example.bookstoreapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.db.SessionManager;
import com.example.bookstoreapp.model.Book;

import java.util.ArrayList;
import java.util.List;

public class BooksActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView rvBooks;
    private TextView tvBookCount;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper       = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        rvBooks     = findViewById(R.id.rvBooks);
        tvBookCount = findViewById(R.id.tvBookCount);

        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));

        loadBooks();
    }

    private void loadBooks() {
        // ── TODO: Replace with db.getAllBooks() when books are added ──
        List<Book> books = getPlaceholderBooks();

        tvBookCount.setText(books.size() + " books available");

        int userId = sessionManager.getUserId();
        BookAdapter adapter = new BookAdapter(this, books, userId, this);
        rvBooks.setAdapter(adapter);
    }

    /**
     * Placeholder books — just for UI display.
     * Replace with DatabaseHelper.getAllBooks() when ready.
     */
    private List<Book> getPlaceholderBooks() {
        List<Book> books = new ArrayList<>();
        String[][] data = {
                {"1",  "Book Title", "Author Name", "#1565C0", "9.99"},
                {"2",  "Book Title", "Author Name", "#2E7D32", "12.99"},
                {"3",  "Book Title", "Author Name", "#B71C1C", "8.99"},
                {"4",  "Book Title", "Author Name", "#6A1B9A", "11.99"},
                {"5",  "Book Title", "Author Name", "#E65100", "10.99"},
                {"6",  "Book Title", "Author Name", "#00695C", "14.99"},
                {"7",  "Book Title", "Author Name", "#4527A0", "13.99"},
                {"8",  "Book Title", "Author Name", "#558B2F", "9.49"},
                {"9",  "Book Title", "Author Name", "#37474F", "10.49"},
        };
        for (String[] d : data) {
            Book b = new Book();
            b.setId(Integer.parseInt(d[0]));
            b.setTitle(d[1]);
            b.setAuthor(d[2]);
            b.setCoverColor(d[3]);
            b.setPrice(Double.parseDouble(d[4]));
            books.add(b);
        }
        return books;
    }

    @Override
    public void onBookClick(Book book) {
        // TODO: Open BookDetailActivity
        Toast.makeText(this, "Book selected", Toast.LENGTH_SHORT).show();
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