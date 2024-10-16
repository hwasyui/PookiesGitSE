package com.example.pookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 2;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_PASSWORD = "password";

    // Feedback table
    private static final String TABLE_FEEDBACK = "feedback";
    private static final String COL_FEEDBACK_ID = "id";
    private static final String COL_FEEDBACK_USER_ID = "userId";
    private static final String COL_FEEDBACK_USERNAME = "username";
    private static final String COL_FEEDBACK_EMAIL = "email";
    private static final String COL_FEEDBACK_TYPE = "feedbackType";
    private static final String COL_FEEDBACK_DESCRIPTION = "description";
    private static final String COL_FEEDBACK_TIME = "feedbackTime";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_EMAIL + " TEXT,"
                + COL_USER_NAME + " TEXT,"
                + COL_USER_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create feedback table
        String CREATE_FEEDBACK_TABLE = "CREATE TABLE " + TABLE_FEEDBACK + "("
                + COL_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FEEDBACK_USER_ID + " TEXT,"
                + COL_FEEDBACK_USERNAME + " TEXT,"
                + COL_FEEDBACK_EMAIL + " TEXT,"
                + COL_FEEDBACK_TYPE + " TEXT,"
                + COL_FEEDBACK_DESCRIPTION + " TEXT,"
                + COL_FEEDBACK_TIME + " TEXT" + ")";
        db.execSQL(CREATE_FEEDBACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDBACK);
        // Create tables again
        onCreate(db);
    }

    // User-related methods

    public boolean insertUser(String email, String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_EMAIL, email);
        contentValues.put(COL_USER_NAME, name);
        contentValues.put(COL_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_EMAIL + "=?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD));
            cursor.close();
            return new User(id, email, name, password);
        }
        return null;
    }

    public boolean updateUsername(String email, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_NAME, newUsername);
        int result = db.update(TABLE_USERS, contentValues, COL_USER_EMAIL + "=?", new String[]{email});
        return result > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_PASSWORD, newPassword);
        int result = db.update(TABLE_USERS, contentValues, COL_USER_EMAIL + "=?", new String[]{email});
        return result > 0;
    }

    // Feedback-related methods

    public long insertFeedback(Feedback feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FEEDBACK_USER_ID, feedback.getUserId());
        values.put(COL_FEEDBACK_USERNAME, feedback.getUsername());
        values.put(COL_FEEDBACK_EMAIL, feedback.getEmail());
        values.put(COL_FEEDBACK_TYPE, feedback.getFeedbackType());
        values.put(COL_FEEDBACK_DESCRIPTION, feedback.getDescription());
        values.put(COL_FEEDBACK_TIME, feedback.getFeedbackTime());
        return db.insert(TABLE_FEEDBACK, null, values);
    }

    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FEEDBACK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Feedback feedback = new Feedback();
                feedback.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_USER_ID)));
                feedback.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_USERNAME)));
                feedback.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_EMAIL)));
                feedback.setFeedbackType(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_TYPE)));
                feedback.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_DESCRIPTION)));
                feedback.setFeedbackTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_TIME)));
                feedbackList.add(feedback);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return feedbackList;
    }

    public List<Feedback> getFeedbackByUserId(String userId) {
        List<Feedback> feedbackList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FEEDBACK, null, COL_FEEDBACK_USER_ID + "=?", new String[]{userId}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Feedback feedback = new Feedback();
                feedback.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_USER_ID)));
                feedback.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_USERNAME)));
                feedback.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_EMAIL)));
                feedback.setFeedbackType(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_TYPE)));
                feedback.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_DESCRIPTION)));
                feedback.setFeedbackTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_FEEDBACK_TIME)));
                feedbackList.add(feedback);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return feedbackList;
    }

    public int deleteFeedback(String userId, String feedbackTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FEEDBACK, COL_FEEDBACK_USER_ID + "=? AND " + COL_FEEDBACK_TIME + "=?", new String[]{userId, feedbackTime});
    }
}