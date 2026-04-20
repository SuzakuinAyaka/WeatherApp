package com.dengyy.weatherapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.constants.Constants;

public class DBHelper extends SQLiteOpenHelper {

    private static volatile DBHelper instance;

    private DBHelper(@Nullable Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(buildUsersTableSql());
        db.execSQL(buildCitiesTableSql());
        db.execSQL(buildCurrentWeatherTableSql());
        db.execSQL(buildForecastWeatherTableSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_FORECAST_WEATHER_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CURRENT_WEATHER_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CITIES);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
        onCreate(db);
    }

    private String buildUsersTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_USERS + " ("
                + Constants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                + Constants.COL_PASSWORD + " TEXT NOT NULL, "
                + Constants.COL_EMAIL + " TEXT, "
                + Constants.COL_PHONE + " TEXT, "
                + Constants.COL_CURRENT_CITY_CODE + " TEXT, "
                + Constants.COL_CREATED_AT + " INTEGER NOT NULL, "
                + Constants.COL_UPDATED_AT + " INTEGER NOT NULL"
                + ")";
    }

    private String buildCitiesTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_CITIES + " ("
                + Constants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COL_USER_ID + " INTEGER NOT NULL, "
                + Constants.COL_CITY_NAME + " TEXT NOT NULL, "
                + Constants.COL_AD_CODE + " TEXT NOT NULL, "
                + Constants.COL_PROVINCE + " TEXT, "
                + Constants.COL_IS_CURRENT + " INTEGER NOT NULL DEFAULT 0, "
                + Constants.COL_CREATED_AT + " INTEGER NOT NULL, "
                + "UNIQUE(" + Constants.COL_USER_ID + ", " + Constants.COL_AD_CODE + ")"
                + ")";
    }

    private String buildCurrentWeatherTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_CURRENT_WEATHER_CACHE + " ("
                + Constants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COL_AD_CODE + " TEXT NOT NULL UNIQUE, "
                + Constants.COL_CITY_NAME + " TEXT, "
                + Constants.COL_WEATHER + " TEXT, "
                + Constants.COL_TEMPERATURE + " TEXT, "
                + Constants.COL_HUMIDITY + " TEXT, "
                + Constants.COL_WIND_DIRECTION + " TEXT, "
                + Constants.COL_WIND_POWER + " TEXT, "
                + Constants.COL_HIGH_TEMP + " TEXT, "
                + Constants.COL_LOW_TEMP + " TEXT, "
                + Constants.COL_REPORT_TIME + " TEXT, "
                + Constants.COL_CACHE_TIME + " INTEGER NOT NULL"
                + ")";
    }

    private String buildForecastWeatherTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_FORECAST_WEATHER_CACHE + " ("
                + Constants.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COL_AD_CODE + " TEXT NOT NULL, "
                + Constants.COL_CITY_NAME + " TEXT, "
                + Constants.COL_FORECAST_DATE + " TEXT NOT NULL, "
                + Constants.COL_WEEK + " TEXT, "
                + Constants.COL_DAY_WEATHER + " TEXT, "
                + Constants.COL_NIGHT_WEATHER + " TEXT, "
                + Constants.COL_DAY_TEMP + " TEXT, "
                + Constants.COL_NIGHT_TEMP + " TEXT, "
                + Constants.COL_DAY_WIND + " TEXT, "
                + Constants.COL_NIGHT_WIND + " TEXT, "
                + Constants.COL_DAY_POWER + " TEXT, "
                + Constants.COL_NIGHT_POWER + " TEXT, "
                + Constants.COL_CACHE_TIME + " INTEGER NOT NULL, "
                + "UNIQUE(" + Constants.COL_AD_CODE + ", " + Constants.COL_FORECAST_DATE + ")"
                + ")";
    }
}
