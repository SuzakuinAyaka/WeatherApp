package com.dengyy.weatherapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.constants.Constants;
import com.dengyy.weatherapp.db.DBHelper;
import com.dengyy.weatherapp.model.User;

public class UserDao {

    private final DBHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_USERNAME, user.getUsername());
        values.put(Constants.COL_PASSWORD, user.getPassword());
        values.put(Constants.COL_EMAIL, user.getEmail());
        values.put(Constants.COL_PHONE, user.getPhone());
        values.put(Constants.COL_CURRENT_CITY_CODE, user.getCurrentCityCode());
        values.put(Constants.COL_CREATED_AT, user.getCreatedAt());
        values.put(Constants.COL_UPDATED_AT, user.getUpdatedAt());
        return db.insert(Constants.TABLE_USERS, null, values);
    }

    public boolean updatePassword(long userId, String passwordHash) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_PASSWORD, passwordHash);
        values.put(Constants.COL_UPDATED_AT, System.currentTimeMillis());
        return db.update(
                Constants.TABLE_USERS,
                values,
                Constants.COL_ID + "=?",
                new String[]{String.valueOf(userId)}
        ) > 0;
    }

    public boolean updateCurrentCity(long userId, String adCode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_CURRENT_CITY_CODE, adCode);
        values.put(Constants.COL_UPDATED_AT, System.currentTimeMillis());
        return db.update(
                Constants.TABLE_USERS,
                values,
                Constants.COL_ID + "=?",
                new String[]{String.valueOf(userId)}
        ) > 0;
    }

    public boolean existsByUsername(String username) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_USERS,
                    new String[]{Constants.COL_ID},
                    Constants.COL_USERNAME + "=?",
                    new String[]{username},
                    null,
                    null,
                    null
            );
            return cursor.moveToFirst();
        } finally {
            closeCursor(cursor);
        }
    }

    @Nullable
    public User findByUsername(String username) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_USERS,
                    null,
                    Constants.COL_USERNAME + "=?",
                    new String[]{username},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return mapUser(cursor);
            }
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    @Nullable
    public User findByIdentity(String username, String identity) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_USERS,
                    null,
                    Constants.COL_USERNAME + "=? AND (" + Constants.COL_EMAIL + "=? OR " + Constants.COL_PHONE + "=?)",
                    new String[]{username, identity, identity},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return mapUser(cursor);
            }
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    @Nullable
    public User findById(long userId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_USERS,
                    null,
                    Constants.COL_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                return mapUser(cursor);
            }
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    private User mapUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_PASSWORD)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_PHONE)));
        user.setCurrentCityCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CURRENT_CITY_CODE)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_CREATED_AT)));
        user.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_UPDATED_AT)));
        return user;
    }

    private void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
