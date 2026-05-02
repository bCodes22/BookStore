package com.example.bookstoreapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.bookstoreapp.model.CartManager;
import com.example.bookstoreapp.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private LinearLayout layoutEmptyCart, layoutBottomBar;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    private CartAdapter adapter;
    private FirestoreHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Cart");
        }

        dbHelper = new FirestoreHelper();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();
        setupCart();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        btnCheckout.setOnClickListener(v -> processMockCheckout());
    }

    private void setupCart() {
        List<Book> cartItems = CartManager.getInstance().getCartItems();

        if (cartItems.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            layoutBottomBar.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            layoutBottomBar.setVisibility(View.VISIBLE);

            adapter = new CartAdapter(cartItems);
            rvCart.setAdapter(adapter);
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = CartManager.getInstance().getTotalPrice();
        tvTotalPrice.setText(String.format("Total: $%.2f", total));
    }

    private void processMockCheckout() {
        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in to checkout.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-clicks
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Processing...");

        // 1. Bundle the cart into an Order object
        List<Book> items = CartManager.getInstance().getCartItems();
        double total = CartManager.getInstance().getTotalPrice();
        long timestamp = System.currentTimeMillis();

        Order newOrder = new Order(currentUserId, items, total, timestamp);

        // 2. Push to Firestore
        dbHelper.placeOrder(newOrder, new FirestoreHelper.OnActionListener() {
            @Override
            public void onSuccess() {
                // 3. Clear the local cart
                CartManager.getInstance().clearCart();
                Toast.makeText(CartActivity.this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();

                // 4. Redirect them straight to their new receipt!
                android.content.Intent intent = new android.content.Intent(CartActivity.this, OrdersActivity.class);
                // This flag ensures that if they hit the "back" button, they don't go back into the empty cart
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                finish(); // Close the cart screen
            }

            @Override
            public void onFailure(Exception e) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Checkout");
                Toast.makeText(CartActivity.this, "Checkout Failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Inner Adapter Class ───────────────────────────────────────────
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

        private final List<Book> items;

        CartAdapter(List<Book> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Book book = items.get(position);

            holder.tvTitle.setText(book.getTitle());
            holder.tvAuthor.setText(book.getAuthor());
            holder.tvPrice.setText(String.format("$%.2f", book.getPrice()));

            if (book.getimageUrl() != null && !book.getimageUrl().isEmpty()) {
                Glide.with(CartActivity.this)
                        .load(book.getimageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.ivCover);
            }

            // Remove item from local cart
            holder.ivRemove.setOnClickListener(v -> {
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
                updateTotal();

                if (items.isEmpty()) {
                    setupCart(); // Trigger empty state UI
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCover, ivRemove;
            TextView tvTitle, tvAuthor, tvPrice;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCover = itemView.findViewById(R.id.ivCartCover);
                ivRemove = itemView.findViewById(R.id.ivCartRemove);
                tvTitle = itemView.findViewById(R.id.tvCartTitle);
                tvAuthor = itemView.findViewById(R.id.tvCartAuthor);
                tvPrice = itemView.findViewById(R.id.tvCartPrice);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}