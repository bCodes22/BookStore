package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.db.SessionManager;
import com.example.bookstoreapp.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

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

        sessionManager = new SessionManager(this);
        dbHelper       = new DatabaseHelper(this);

        // Fetch full user from DB for createdAt
        String email = sessionManager.getUserEmail();
        User user    = dbHelper.getUserByEmail(email);

        // ── Views ──────────────────────────────────────────
        TextView tvAvatar      = findViewById(R.id.tvAvatar);
        TextView tvName        = findViewById(R.id.tvName);
        TextView tvEmail       = findViewById(R.id.tvEmail);
        TextView tvAuthType    = findViewById(R.id.tvAuthType);
        TextView tvMemberSince = findViewById(R.id.tvMemberSince);
        TextView tvSession     = findViewById(R.id.tvSession);
        Button   btnLogout     = findViewById(R.id.btnLogout);

        // ── Populate ───────────────────────────────────────
        String name = sessionManager.getUserName();

        // Initials avatar — first letter of name, uppercased
        tvAvatar.setText(name.substring(0, 1).toUpperCase());

        tvName.setText(name);
        tvEmail.setText(sessionManager.getUserEmail());

        // Auth type badge
        String authType = sessionManager.getAuthType();
        tvAuthType.setText("google".equals(authType) ? "Google Account" : "Email Account");

        // Member since — from DB createdAt timestamp
        if (user != null && user.getCreatedAt() > 0) {
            String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(user.getCreatedAt()));
            tvMemberSince.setText(date);
        } else {
            tvMemberSince.setText("N/A");
        }

        // Session expiry
        tvSession.setText(sessionManager.getSessionExpiryInfo());

        // ── Logout ─────────────────────────────────────────
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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