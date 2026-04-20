package com.dengyy.weatherapp.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.constants.Constants;
import com.dengyy.weatherapp.db.DBHelper;
import com.dengyy.weatherapp.model.ForecastWeather;

import java.util.ArrayList;
import java.util.List;

public class ForecastWeatherDao {

    private final DBHelper dbHelper;

    public ForecastWeatherDao(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public void replaceForecasts(String adCode, List<ForecastWeather> forecasts) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Constants.TABLE_FORECAST_WEATHER_CACHE, Constants.COL_AD_CODE + "=?", new String[]{adCode});
            for (ForecastWeather forecast : forecasts) {
                ContentValues values = new ContentValues();
                values.put(Constants.COL_AD_CODE, forecast.getAdCode());
                values.put(Constants.COL_CITY_NAME, forecast.getCityName());
                values.put(Constants.COL_FORECAST_DATE, forecast.getForecastDate());
                values.put(Constants.COL_WEEK, forecast.getWeek());
                values.put(Constants.COL_DAY_WEATHER, forecast.getDayWeather());
                values.put(Constants.COL_NIGHT_WEATHER, forecast.getNightWeather());
                values.put(Constants.COL_DAY_TEMP, forecast.getDayTemp());
                values.put(Constants.COL_NIGHT_TEMP, forecast.getNightTemp());
                values.put(Constants.COL_DAY_WIND, forecast.getDayWind());
                values.put(Constants.COL_NIGHT_WIND, forecast.getNightWind());
                values.put(Constants.COL_DAY_POWER, forecast.getDayPower());
                values.put(Constants.COL_NIGHT_POWER, forecast.getNightPower());
                values.put(Constants.COL_CACHE_TIME, forecast.getCacheTime());
                db.insert(Constants.TABLE_FORECAST_WEATHER_CACHE, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<ForecastWeather> getByAdCode(String adCode) {
        List<ForecastWeather> forecasts = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(
                    Constants.TABLE_FORECAST_WEATHER_CACHE,
                    null,
                    Constants.COL_AD_CODE + "=?",
                    new String[]{adCode},
                    null,
                    null,
                    Constants.COL_FORECAST_DATE + " ASC"
            );
            while (cursor.moveToNext()) {
                forecasts.add(mapForecast(cursor));
            }
        } finally {
            closeCursor(cursor);
        }
        return forecasts;
    }

    private ForecastWeather mapForecast(Cursor cursor) {
        ForecastWeather forecast = new ForecastWeather();
        forecast.setId(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_ID)));
        forecast.setAdCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_AD_CODE)));
        forecast.setCityName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_CITY_NAME)));
        forecast.setForecastDate(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_FORECAST_DATE)));
        forecast.setWeek(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_WEEK)));
        forecast.setDayWeather(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DAY_WEATHER)));
        forecast.setNightWeather(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_NIGHT_WEATHER)));
        forecast.setDayTemp(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DAY_TEMP)));
        forecast.setNightTemp(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_NIGHT_TEMP)));
        forecast.setDayWind(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DAY_WIND)));
        forecast.setNightWind(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_NIGHT_WIND)));
        forecast.setDayPower(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_DAY_POWER)));
        forecast.setNightPower(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_NIGHT_POWER)));
        forecast.setCacheTime(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COL_CACHE_TIME)));
        return forecast;
    }

    private void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
