package com.example.bookstoreapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bookstoreapp.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DatabaseHelper — Local SQLite implementation.
 *
 * AWS MIGRATION GUIDE:
 * ─────────────────────────────────────────────────────────────────────
 * When switching to AWS, replace each method body with Retrofit/Amplify
 * API calls. Keep the same method signatures so callers don't change.
 *
 * Recommended AWS Stack:
 *   - AWS Amplify Auth (Cognito User Pools) → loginUser(), registerUser()
 *   - AWS DynamoDB or RDS via API Gateway → getUserByEmail()
 *   - AWS SDK for Android (com.amazonaws:aws-android-sdk-core)
 *
 * Steps:
 *   1. Add Amplify/AWS SDK dependencies to build.gradle
 *   2. Replace method bodies with async Amplify.Auth calls
 *   3. SessionManager.createSession() stays the same
 * ─────────────────────────────────────────────────────────────────────
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_AUTH_TYPE = "auth_type";
    public static final String COL_CREATED_AT = "created_at";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_PASSWORD + " TEXT NOT NULL, " +
                    COL_AUTH_TYPE + " TEXT DEFAULT 'email', " +
                    COL_CREATED_AT + " INTEGER" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Register a new user.
     * AWS: Replace with Amplify.Auth.signUp()
     */
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, user.getName());
        values.put(COL_EMAIL, user.getEmail().toLowerCase());
        values.put(COL_PASSWORD, hashPassword(user.getPassword()));
        values.put(COL_AUTH_TYPE, user.getAuthType());
        values.put(COL_CREATED_AT, System.currentTimeMillis());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Login user with email + password.
     * AWS: Replace with Amplify.Auth.signIn()
     */
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email.toLowerCase(), hashedPassword},
                null, null, null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    /**
     * Check if an email is already registered.
     * AWS: Replace with a check against Cognito user pool or API call.
     */
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS, new String[]{COL_ID},
                COL_EMAIL + "=?",
                new String[]{email.toLowerCase()},
                null, null, null
        );
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    /**
     * Get user by email (used for Google sign-in upsert).
     * AWS: Replace with Cognito getUser() or DynamoDB query.
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS, null,
                COL_EMAIL + "=?",
                new String[]{email.toLowerCase()},
                null, null, null
        );
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
        user.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndex(COL_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndex(COL_PASSWORD)));
        user.setAuthType(cursor.getString(cursor.getColumnIndex(COL_AUTH_TYPE)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndex(COL_CREATED_AT)));
        return user;
    }

    /**
     * SHA-256 password hashing.
     * AWS: Cognito handles password hashing internally — remove this when migrating.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password; // fallback (should never happen)
        }
    }
}