package com.dengyy.weatherapp.ui;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.dengyy.weatherapp.R;

public final class WeatherThemeResolver {

    private WeatherThemeResolver() {
    }

    public static int resolveWeatherType(@Nullable String weather) {
        String value = normalize(weather);
        if (value == null) {
            return WeatherBackgroundView.WEATHER_SUNNY;
        }
        if (containsAny(value, "雪", "冻雨", "冷")) {
            return WeatherBackgroundView.WEATHER_SNOW;
        }
        if (containsAny(value, "雨", "雷", "雹")) {
            return WeatherBackgroundView.WEATHER_RAIN;
        }
        if (containsAny(value, "阴", "云", "雾", "霾", "尘", "沙", "风", "龙卷")) {
            return WeatherBackgroundView.WEATHER_CLOUDY;
        }
        if (containsAny(value, "晴", "热")) {
            return WeatherBackgroundView.WEATHER_SUNNY;
        }
        return WeatherBackgroundView.WEATHER_CLOUDY;
    }

    @NonNull
    public static int[] resolveGradientColors(@NonNull Context context, @Nullable String weather) {
        switch (resolveWeatherType(weather)) {
            case WeatherBackgroundView.WEATHER_RAIN:
                return new int[]{
                        ContextCompat.getColor(context, R.color.main_bg_rain_top),
                        ContextCompat.getColor(context, R.color.main_bg_rain_bottom)
                };
            case WeatherBackgroundView.WEATHER_SNOW:
                return new int[]{
                        ContextCompat.getColor(context, R.color.main_bg_snow_top),
                        ContextCompat.getColor(context, R.color.main_bg_snow_bottom)
                };
            case WeatherBackgroundView.WEATHER_CLOUDY:
                return new int[]{
                        ContextCompat.getColor(context, R.color.main_bg_cloudy_top),
                        ContextCompat.getColor(context, R.color.main_bg_cloudy_bottom)
                };
            case WeatherBackgroundView.WEATHER_SUNNY:
            default:
                return new int[]{
                        ContextCompat.getColor(context, R.color.main_bg_sunny_top),
                        ContextCompat.getColor(context, R.color.main_bg_sunny_bottom)
                };
        }
    }

    @Nullable
    private static String normalize(@Nullable String weather) {
        if (TextUtils.isEmpty(weather)) {
            return null;
        }
        return weather.trim();
    }

    private static boolean containsAny(@NonNull String source, @NonNull String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
