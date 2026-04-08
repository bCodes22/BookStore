package com.example.bookstoreapp.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.bookstoreapp.model.User;

/**
 * SessionManager — Manages user login session with 15-day expiry.
 *
 * AWS MIGRATION GUIDE:
 * ─────────────────────────────────────────────────────────────────────
 * When switching to AWS Cognito:
 *   - Cognito tokens (AccessToken, IdToken, RefreshToken) are stored
 *     automatically by Amplify. The RefreshToken typically lasts 30 days
 *     and handles re-authentication transparently.
 *   - You can still use this SessionManager for user profile caching.
 *   - isLoggedIn() → check Amplify.Auth.getCurrentUser() instead.
 *   - logout() → replace with Amplify.Auth.signOut()
 * ─────────────────────────────────────────────────────────────────────
 */
public class SessionManager {

    private static final String PREF_NAME = "BookStoreSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_AUTH_TYPE = "authType";
    private static final String KEY_LOGIN_TIME = "loginTime";

    // 15 days in milliseconds
    private static final long SESSION_DURATION = 15L * 24 * 60 * 60 * 1000;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Create a new session for a successfully authenticated user.
     * Stores user details locally and records login timestamp.
     */
    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_AUTH_TYPE, user.getAuthType());
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if the user has a valid, non-expired session.
     * Session is valid for 15 days from login time.
     */
    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!loggedIn) return false;

        long loginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - loginTime;

        if (elapsed > SESSION_DURATION) {
            // Session expired — auto logout
            logout();
            return false;
        }
        return true;
    }

    /**
     * Get remaining session time in a readable format.
     */
    public String getSessionExpiryInfo() {
        long loginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        long expiryTime = loginTime + SESSION_DURATION;
        long remaining = expiryTime - System.currentTimeMillis();

        if (remaining <= 0) return "Session expired";

        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);

        if (days > 0) {
            return "Session valid for " + days + " day" + (days > 1 ? "s" : "");
        } else {
            return "Session expires in " + hours + " hour" + (hours > 1 ? "s" : "");
        }
    }

    /**
     * Refresh session timestamp (extend session on active use).
     * Call this on each app open to keep active users logged in.
     */
    public void refreshSession() {
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }

    /**
     * Clear all session data and log the user out.
     * AWS: Replace with Amplify.Auth.signOut()
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // ─── Getters ───────────────────────────────────────────────────────

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getAuthType() {
        return prefs.getString(KEY_AUTH_TYPE, "email");
    }
}