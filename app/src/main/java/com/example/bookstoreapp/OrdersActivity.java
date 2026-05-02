package com.example.bookstoreapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstoreapp.db.FirestoreHelper;
import com.example.bookstoreapp.model.Book;
import com.example.bookstoreapp.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmptyOrders;
    private FirestoreHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Order History");
        }

        rvOrders = findViewById(R.id.rvOrders);
        layoutEmptyOrders = findViewById(R.id.layoutEmptyOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new FirestoreHelper();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            loadOrders();
        } else {
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            loadOrders();
        }
    }
    private void loadOrders() {
        dbHelper.getUserOrders(currentUserId, new FirestoreHelper.OnOrdersLoadedListener() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (orders.isEmpty()) {
                    layoutEmptyOrders.setVisibility(View.VISIBLE);
                    rvOrders.setVisibility(View.GONE);
                } else {
                    layoutEmptyOrders.setVisibility(View.GONE);
                    rvOrders.setVisibility(View.VISIBLE);
                    rvOrders.setAdapter(new OrderAdapter(orders));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // INNER ADAPTER CLASS
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private final List<Order> orders;
        private final SimpleDateFormat dateFormat;

        OrderAdapter(List<Order> orders) {
            this.orders = orders;
            this.dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);

            // Format Date
            Date date = new Date(order.getOrderDate());
            holder.tvOrderDate.setText(dateFormat.format(date));

            // Format Price
            holder.tvOrderTotal.setText(String.format("Total: $%.2f", order.getTotalPrice()));
            holder.tvOrderId.setText("Order ID: " + order.getId());

            // Build a comma separated list of book titles
            StringBuilder itemsText = new StringBuilder();
            if (order.getItems() != null) {
                for (int i = 0; i < order.getItems().size(); i++) {
                    itemsText.append(order.getItems().get(i).getTitle());
                    if (i < order.getItems().size() - 1) {
                        itemsText.append(", ");
                    }
                }
            }
            holder.tvOrderItems.setText(itemsText.toString());
        }

        @Override
        public int getItemCount() { return orders.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderDate, tvOrderTotal, tvOrderId, tvOrderItems;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        return true;
    }
}