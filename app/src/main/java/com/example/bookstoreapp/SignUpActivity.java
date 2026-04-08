package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.db.SessionManager;
import com.example.bookstoreapp.model.User;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnGoogleSignUp;
    private TextView tvLoginLink;
    private CheckBox cbTerms;
    private ProgressBar progressBar;
    private LinearLayout formContainer;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

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

        tvLoginLink.setOnClickListener(v -> {
            onBackPressed();
        });

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
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            shakeView(etEmail);
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            shakeView(etEmail);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            shakeView(etPassword);
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
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
        String finalEmail = email;
        String finalPassword = password;
        String finalName = name;

        new android.os.Handler().postDelayed(() -> {
            if (dbHelper.isEmailRegistered(finalEmail)) {
                showLoading(false);
                etEmail.setError("Email already registered");
                etEmail.requestFocus();
                shakeView(etEmail);
                return;
            }

            User newUser = new User(finalName, finalEmail, finalPassword, "email");
            long id = dbHelper.registerUser(newUser);
            showLoading(false);

            if (id != -1) {
                newUser.setId((int) id);
                sessionManager.createSession(newUser);
                Toast.makeText(SignUpActivity.this, "Account created! Welcome, " + finalName + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }, 800);
    }

    private void handleGoogleSignUp() {
        showLoading(true);
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            String googleEmail = "google.user@gmail.com";
            String googleName = "Google User";

            User existingUser = dbHelper.getUserByEmail(googleEmail);
            if (existingUser == null) {
                User googleUser = new User(googleName, googleEmail, "GOOGLE_AUTH", "google");
                long id = dbHelper.registerUser(googleUser);
                googleUser.setId((int) id);
                sessionManager.createSession(googleUser);
            } else {
                sessionManager.createSession(existingUser);
            }

            Toast.makeText(SignUpActivity.this, "Signed up with Google!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 1200);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
        btnGoogleSignUp.setEnabled(!show);
    }

    private void animateButton(View view) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
        view.startAnimation(pulse);
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}