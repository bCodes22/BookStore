package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// Firebase & Google Auth Imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    // UI Elements
    private TextView tvAvatar, tvName, tvEmail, tvAuthType, tvMemberSince, tvSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign-In (needed for logging out completely)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Security check
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            logoutAndRedirect();
            return;
        }

        initViews();

        // Immediately set what we know from the Auth token
        tvEmail.setText(currentUser.getEmail());
        tvSession.setText("Active (Managed by Firebase)");

        // Fetch the rest of the profile from Firestore
        loadProfileFromFirestore(currentUser.getUid());

        setupLogoutButton();
    }

    private void initViews() {
        tvAvatar      = findViewById(R.id.tvAvatar);
        tvName        = findViewById(R.id.tvName);
        tvEmail       = findViewById(R.id.tvEmail);
        tvAuthType    = findViewById(R.id.tvAuthType);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvSession     = findViewById(R.id.tvSession);

        // Set loading defaults
        tvName.setText("Loading...");
        tvAuthType.setText("Loading...");
        tvMemberSince.setText("Loading...");
    }

    private void loadProfileFromFirestore(String uid) {
        db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String authType = documentSnapshot.getString("authType");
                        Long createdAt = documentSnapshot.getLong("createdAt");

                        // Avatar Initials
                        if (name != null && !name.isEmpty()) {
                            tvName.setText(name);
                            tvAvatar.setText(name.substring(0, 1).toUpperCase());
                        } else {
                            tvName.setText("User");
                            tvAvatar.setText("U");
                        }

                        // Auth Type Badge
                        tvAuthType.setText("google".equals(authType) ? "Google Account" : "Email Account");

                        // Member Since
                        if (createdAt != null && createdAt > 0) {
                            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(new Date(createdAt));
                            tvMemberSince.setText(date);
                        } else {
                            tvMemberSince.setText("N/A");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        // 1. Sign out of Firebase Auth
                        mAuth.signOut();

                        // 2. Sign out of Google to clear the saved account picker state
                        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                            logoutAndRedirect();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void logoutAndRedirect() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
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