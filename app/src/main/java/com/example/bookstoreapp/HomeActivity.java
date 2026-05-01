package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

// Firebase & Google Auth Imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    // Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    // UI Elements that need dynamic data
    private TextView tvNavName;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In client (required for logging out)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Security Check: Ensure user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            logoutAndRedirect();
            return;
        }

        // 3. UI Setup
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

        // 4. Setup Nav Header and Welcome Message
        View headerView = navigationView.getHeaderView(0);
        tvNavName  = headerView.findViewById(R.id.tvNavUserName);
        TextView tvNavEmail = headerView.findViewById(R.id.tvNavUserEmail);
        tvWelcome = findViewById(R.id.tvWelcome);

        // Set the email immediately from the Auth token
        tvNavEmail.setText(currentUser.getEmail());

        // Default text while loading
        tvNavName.setText("Loading...");
        tvWelcome.setText("Welcome back! 👋");

        // Fetch the user's name from Firestore
        loadUserData(currentUser.getUid());

        // 5. Quick Action cards
        setupQuickActionCards();
    }

    private void loadUserData(String uid) {
        db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvNavName.setText(name);
                            tvWelcome.setText("Welcome back,\n" + name + "! 👋");
                        } else {
                            tvNavName.setText("User");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "Error fetching user data", e));
    }

    private void setupQuickActionCards() {
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
            // 1. Sign out of Firebase
            mAuth.signOut();
            // 2. Sign out of Google Client (so they aren't auto-logged into the same account next time)
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                logoutAndRedirect();
            });
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutAndRedirect() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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