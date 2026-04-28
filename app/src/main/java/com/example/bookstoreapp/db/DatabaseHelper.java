package com.example.bookstoreapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bookstoreapp.model.Book;
import com.example.bookstoreapp.model.User;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 2;

    // ── Users ────────────────────────────────────────────────
    public static final String TABLE_USERS    = "users";
    public static final String COL_ID         = "id";
    public static final String COL_NAME       = "name";
    public static final String COL_EMAIL      = "email";
    public static final String COL_PASSWORD   = "password";
    public static final String COL_AUTH_TYPE  = "auth_type";
    public static final String COL_CREATED_AT = "created_at";

    // ── Books ────────────────────────────────────────────────
    public static final String TABLE_BOOKS           = "books";
    public static final String COL_BOOK_TITLE        = "title";
    public static final String COL_BOOK_AUTHOR       = "author";
    public static final String COL_BOOK_COVER_URL    = "cover_url";   // image link goes here
    public static final String COL_BOOK_PRICE        = "price";
    public static final String COL_BOOK_DESCRIPTION  = "description";

    // ── Wishlist ─────────────────────────────────────────────
    public static final String TABLE_WISHLIST     = "wishlist";
    public static final String COL_WISH_USER_ID   = "user_id";
    public static final String COL_WISH_BOOK_ID   = "book_id";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME       + " TEXT NOT NULL, " +
                    COL_EMAIL      + " TEXT UNIQUE NOT NULL, " +
                    COL_PASSWORD   + " TEXT NOT NULL, " +
                    COL_AUTH_TYPE  + " TEXT DEFAULT 'email', " +
                    COL_CREATED_AT + " INTEGER)";

    private static final String CREATE_BOOKS_TABLE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COL_ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BOOK_TITLE       + " TEXT NOT NULL, " +
                    COL_BOOK_AUTHOR      + " TEXT NOT NULL, " +
                    COL_BOOK_COVER_URL   + " TEXT, " +
                    COL_BOOK_PRICE       + " REAL NOT NULL, " +
                    COL_BOOK_DESCRIPTION + " TEXT)";

    private static final String CREATE_WISHLIST_TABLE =
            "CREATE TABLE " + TABLE_WISHLIST + " (" +
                    COL_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_WISH_USER_ID + " INTEGER NOT NULL, " +
                    COL_WISH_BOOK_ID + " INTEGER NOT NULL, " +
                    "UNIQUE(" + COL_WISH_USER_ID + ", " + COL_WISH_BOOK_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);
        db.execSQL(CREATE_WISHLIST_TABLE);
        // No sample data — books will be added later
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Preserve existing users, just add new tables
            db.execSQL(CREATE_BOOKS_TABLE);
            db.execSQL(CREATE_WISHLIST_TABLE);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Books
    // ─────────────────────────────────────────────────────────
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do { books.add(cursorToBook(cursor)); } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return books;
    }

    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_TITLE)));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_AUTHOR)));
        book.setCoverUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_COVER_URL)));
        book.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BOOK_PRICE)));
        book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_DESCRIPTION)));
        return book;
    }

    // ─────────────────────────────────────────────────────────
    // Wishlist
    // ─────────────────────────────────────────────────────────
    public boolean addToWishlist(int userId, int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WISH_USER_ID, userId);
        values.put(COL_WISH_BOOK_ID, bookId);
        long result = db.insertWithOnConflict(TABLE_WISHLIST, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    public boolean removeFromWishlist(int userId, int bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_WISHLIST,
                COL_WISH_USER_ID + "=? AND " + COL_WISH_BOOK_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        db.close();
        return rows > 0;
    }

    public boolean isInWishlist(int userId, int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WISHLIST, new String[]{COL_ID},
                COL_WISH_USER_ID + "=? AND " + COL_WISH_BOOK_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public List<Book> getWishlistBooks(int userId) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT b.* FROM " + TABLE_BOOKS + " b " +
                        "INNER JOIN " + TABLE_WISHLIST + " w ON b." + COL_ID + " = w." + COL_WISH_BOOK_ID + " " +
                        "WHERE w." + COL_WISH_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do { books.add(cursorToBook(cursor)); } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return books;
    }

    // ─────────────────────────────────────────────────────────
    // Users (unchanged)
    // ─────────────────────────────────────────────────────────
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME,       user.getName());
        values.put(COL_EMAIL,      user.getEmail().toLowerCase());
        values.put(COL_PASSWORD,   hashPassword(user.getPassword()));
        values.put(COL_AUTH_TYPE,  user.getAuthType());
        values.put(COL_CREATED_AT, System.currentTimeMillis());
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email.toLowerCase(), hashPassword(password)},
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_EMAIL + "=?", new String[]{email.toLowerCase()},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_EMAIL + "=?", new String[]{email.toLowerCase()},
                null, null, null);
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
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)));
        user.setAuthType(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_TYPE)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        return user;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return password;
        }
    }
}