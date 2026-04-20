package com.dengyy.weatherapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.constants.Constants;
import com.dengyy.weatherapp.db.DBHelper;
import com.dengyy.weatherapp.model.City;

import java.util.ArrayList;
import java.util.List;

public class CityDao {

    private final DBHelper dbHelper;

    public CityDao(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public long insert(City city) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_USER_ID, city.getUserId());
        values.put(Constants.COL_CITY_NAME, city.getCityName());
        values.put(Constants.COL_AD_CODE, city.getAdCode());
        values.put(Constants.COL_PROVINCE, city.getProvince());
        values.put(Constants.COL_IS_CURRENT, city.isCurrent() ? 1 : 0);
        values.put(Constants.COL_CREATED_AT, city.getCreatedAt());
        return db.insertWithOnConflict(Constants.TABLE_CITIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<City> getCitiesByUserId(long userId) {
        List<City> cities = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_CITIES,
                    null,
                    Constants.COL_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    Constants.COL_IS_CURRENT + " DESC, " + Constants.COL_CREATED_AT + " ASC"
            );
            while (cursor.moveToNext()) {
                cities.add(mapCity(cursor));
            }
        } finally {
            closeCursor(cursor);
        }
        return cities;
    }

    public boolean setCurrentCity(long userId, String adCode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues clearValues = new ContentValues();
            clearValues.put(Constants.COL_IS_CURRENT, 0);
            db.update(
                    Constants.TABLE_CITIES,
                    clearValues,
                    Constants.COL_USER_ID + "=?",
                    new String[]{String.valueOf(userId)}
            );

            ContentValues currentValues = new ContentValues();
            currentValues.put(Constants.COL_IS_CURRENT, 1);
            int affected = db.update(
                    Constants.TABLE_CITIES,
                    currentValues,
                    Constants.COL_USER_ID + "=? AND " + Constants.COL_AD_CODE + "=?",
                    new String[]{String.valueOf(userId), adCode}
            );
            db.setTransactionSuccessful();
            return affected > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteCity(long userId, String adCode) {
        return dbHelper.getWritableDatabase().delete(
                Constants.TABLE_CITIES,
                Constants.COL_USER_ID + "=? AND " + Constants.COL_AD_CODE + "=?",
                new String[]{String.valueOf(userId), adCode}
        ) > 0;
    }

    @Nullable
    public City getCurrentCity(long userId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_CITIES,
                    null,
                    Constants.COL_USER_ID + "=? AND " + Constants.COL_IS_CURRENT + "=1",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    Constants.COL_CREATED_AT + " ASC",
                    "1"
            );
            if (cursor.moveToFirst()) {
                return mapCity(cursor);
            }
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    private City mapCity(Cursor cursor) {
        City city = new City();
        city.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_ID)));
        city.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_USER_ID)));
        city.setCityName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CITY_NAME)));
        city.setAdCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_AD_CODE)));
        city.setProvince(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_PROVINCE)));
        city.setCurrent(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COL_IS_CURRENT)) == 1);
        city.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_CREATED_AT)));
        return city;
    }

    private void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
