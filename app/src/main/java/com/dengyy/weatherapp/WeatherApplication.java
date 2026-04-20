package com.dengyy.weatherapp;

import android.app.Application;

import com.dengyy.weatherapp.db.DBHelper;

public class WeatherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DBHelper.getInstance(this).getWritableDatabase();
    }
}
