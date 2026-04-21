package com.dengyy.weatherapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.dengyy.weatherapp.db.DBHelper;
import com.dengyy.weatherapp.utils.SPUtils;

public class WeatherApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        applyThemeMode();
        DBHelper.getInstance(this).getWritableDatabase();
    }

    private void applyThemeMode() {
        if (SPUtils.isThemeFollowSystem(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            return;
        }
        int savedThemeMode = SPUtils.getThemeMode(this);
        AppCompatDelegate.setDefaultNightMode(
                savedThemeMode == 1
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
