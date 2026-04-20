package com.dengyy.weatherapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.constants.Constants;
import com.dengyy.weatherapp.db.DBHelper;
import com.dengyy.weatherapp.model.CurrentWeather;

public class CurrentWeatherDao {

    private final DBHelper dbHelper;

    public CurrentWeatherDao(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public long insertOrReplace(CurrentWeather weather) {
        ContentValues values = new ContentValues();
        values.put(Constants.COL_AD_CODE, weather.getAdCode());
        values.put(Constants.COL_CITY_NAME, weather.getCityName());
        values.put(Constants.COL_WEATHER, weather.getWeather());
        values.put(Constants.COL_TEMPERATURE, weather.getTemperature());
        values.put(Constants.COL_HUMIDITY, weather.getHumidity());
        values.put(Constants.COL_WIND_DIRECTION, weather.getWindDirection());
        values.put(Constants.COL_WIND_POWER, weather.getWindPower());
        values.put(Constants.COL_HIGH_TEMP, weather.getHighTemp());
        values.put(Constants.COL_LOW_TEMP, weather.getLowTemp());
        values.put(Constants.COL_REPORT_TIME, weather.getReportTime());
        values.put(Constants.COL_CACHE_TIME, weather.getCacheTime());
        return dbHelper.getWritableDatabase().insertWithOnConflict(
                Constants.TABLE_CURRENT_WEATHER_CACHE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    @Nullable
    public CurrentWeather getByAdCode(String adCode) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_CURRENT_WEATHER_CACHE,
                    null,
                    Constants.COL_AD_CODE + "=?",
                    new String[]{adCode},
                    null,
                    null,
                    null,
                    "1"
            );
            if (cursor.moveToFirst()) {
                return mapWeather(cursor);
            }
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    private CurrentWeather mapWeather(Cursor cursor) {
        CurrentWeather weather = new CurrentWeather();
        weather.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_ID)));
        weather.setAdCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_AD_CODE)));
        weather.setCityName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CITY_NAME)));
        weather.setWeather(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_WEATHER)));
        weather.setTemperature(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_TEMPERATURE)));
        weather.setHumidity(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_HUMIDITY)));
        weather.setWindDirection(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_WIND_DIRECTION)));
        weather.setWindPower(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_WIND_POWER)));
        weather.setHighTemp(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_HIGH_TEMP)));
        weather.setLowTemp(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_LOW_TEMP)));
        weather.setReportTime(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_REPORT_TIME)));
        weather.setCacheTime(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_CACHE_TIME)));
        return weather;
    }

    private void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
