package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bookstoreapp.db.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Nav header
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavName  = headerView.findViewById(R.id.tvNavUserName);
        TextView tvNavEmail = headerView.findViewById(R.id.tvNavUserEmail);
        tvNavName.setText(sessionManager.getUserName());
        tvNavEmail.setText(sessionManager.getUserEmail());

        // Welcome message
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome back,\n" + sessionManager.getUserName() + "! 👋");

        // Quick Action cards
        LinearLayout cardBooks    = findViewById(R.id.cardBooks);
        LinearLayout cardOrders   = findViewById(R.id.cardOrders);
        LinearLayout cardWishlist = findViewById(R.id.cardWishlist);
        LinearLayout cardProfile  = findViewById(R.id.cardProfile);

        cardBooks.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, BooksActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardOrders.setOnClickListener(v ->
                Toast.makeText(this, "Orders - Coming Soon", Toast.LENGTH_SHORT).show());

        cardWishlist.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WishlistActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Already home
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_orders) {
            Toast.makeText(this, "Orders - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_wishlist) {
            startActivity(new Intent(this, WishlistActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_logout) {
            sessionManager.logout();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}