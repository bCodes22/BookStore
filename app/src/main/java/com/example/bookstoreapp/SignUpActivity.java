package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// Firebase & Google Auth Imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnGoogleSignUp;
    private TextView tvLoginLink;
    private CheckBox cbTerms;
    private ProgressBar progressBar;
    private LinearLayout formContainer;

    // Firebase variables replacing DatabaseHelper
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        startEntryAnimations();
        setListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        cbTerms = findViewById(R.id.cbTerms);
        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
    }

    private void startEntryAnimations() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        formContainer.startAnimation(slideInRight);
        btnGoogleSignUp.startAnimation(fadeIn);
    }

    private void setListeners() {
        btnSignUp.setOnClickListener(v -> {
            animateButton(btnSignUp);
            handleSignUp();
        });

        btnGoogleSignUp.setOnClickListener(v -> {
            animateButton(btnGoogleSignUp);
            handleGoogleSignUp();
        });

        tvLoginLink.setOnClickListener(v -> onBackPressed());

        // Live password strength
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }
        });
    }

    private void updatePasswordStrength(String password) {
        TextView tvStrength = findViewById(R.id.tvPasswordStrength);
        if (password.isEmpty()) {
            tvStrength.setVisibility(View.GONE);
            return;
        }
        tvStrength.setVisibility(View.VISIBLE);
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()].*")) strength++;

        switch (strength) {
            case 0:
            case 1:
                tvStrength.setText("Weak password");
                tvStrength.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case 2:
                tvStrength.setText("Fair password");
                tvStrength.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                break;
            case 3:
                tvStrength.setText("Good password");
                tvStrength.setTextColor(0xFF4CAF50);
                break;
            case 4:
                tvStrength.setText("Strong password ✓");
                tvStrength.setTextColor(0xFF2E7D32);
                break;
        }
    }

    private void handleSignUp() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Standard UI Validation
        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            shakeView(etFullName);
            return;
        }
        if (name.length() < 2) {
            etFullName.setError("Name must be at least 2 characters");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            shakeView(etEmail);
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            shakeView(etPassword);
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            shakeView(etConfirmPassword);
            return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            shakeView(cbTerms);
            return;
        }

        showLoading(true);

        // Real Firebase Registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToFirestore(user, name, "email");
                    } else {
                        showLoading(false);
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            etEmail.setError("Email already registered");
                            etEmail.requestFocus();
                            shakeView(etEmail);
                        } catch (Exception e) {
                            Toast.makeText(SignUpActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void handleGoogleSignUp() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                showLoading(false);
                Log.w("GoogleAuth", "Google sign up failed", e);
                Toast.makeText(this, "Google Sign-Up Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Google provides the name automatically
                        saveUserToFirestore(user, user.getDisplayName(), "google");
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handles writing the profile to the database for both Email and Google signups
    private void saveUserToFirestore(FirebaseUser user, String name, String authType) {
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("email", user.getEmail());
        userProfile.put("authType", authType);
        userProfile.put("createdAt", System.currentTimeMillis());

        // SetOptions.merge() ensures we don't accidentally wipe existing data
        // if a user clicks "Sign Up with Google" but already has an account.
        db.collection("Users").document(user.getUid())
                .set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "Account created! Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                    goToHomeActivity();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "Failed to create profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
        btnGoogleSignUp.setEnabled(!show);
        etFullName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
        cbTerms.setEnabled(!show);
    }

    private void animateButton(View view) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
        view.startAnimation(pulse);
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}