package com.example.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstoreapp.db.DatabaseHelper;
import com.example.bookstoreapp.db.SessionManager;
import com.example.bookstoreapp.model.User;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private LinearLayout formContainer;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase();
        dbHelper.registerUser(
                new User("Test", "test@test.com", "123456", "email")
        );
        sessionManager = new SessionManager(this);
        File dbFile = getDatabasePath("bookstore.db");
        Log.d("DB_PATH", dbFile.getAbsolutePath());
        Log.d("DB_EXISTS", String.valueOf(dbFile.exists()));
        initViews();
        startEntryAnimations();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        formContainer = findViewById(R.id.formContainer);
    }

    private void startEntryAnimations() {
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        formContainer.startAnimation(slideInLeft);
        btnGoogleSignIn.startAnimation(fadeIn);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> {
            animateButton(btnLogin);
            handleLogin();
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            animateButton(btnGoogleSignIn);
            handleGoogleSignIn();
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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
            shakeView(etPassword);
            return;
        }

        showLoading(true);

        // Simulate network delay for future AWS integration
        new android.os.Handler().postDelayed(() -> {
            User user = dbHelper.loginUser(email, password);
            showLoading(false);

            if (user != null) {
                sessionManager.createSession(user);
                Toast.makeText(LoginActivity.this, "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                shakeView(etPassword);
                etPassword.setText("");
            }
        }, 800);
    }

    private void handleGoogleSignIn() {
        showLoading(true);
        // Simulated Google Sign-In - ready to replace with actual Google Auth SDK
        // For AWS: replace with Cognito Hosted UI or Google Identity Services
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            // Simulate a Google user
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

            Toast.makeText(LoginActivity.this, "Signed in with Google!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 1200);
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email address");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);
        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this,
                        "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnGoogleSignIn.setEnabled(!show);
    }

    private void animateButton(View view) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
        view.startAnimation(pulse);
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }
}