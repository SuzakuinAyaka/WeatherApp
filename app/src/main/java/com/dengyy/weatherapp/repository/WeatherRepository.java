package com.dengyy.weatherapp.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.db.dao.CurrentWeatherDao;
import com.dengyy.weatherapp.db.dao.ForecastWeatherDao;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.ForecastWeather;

import java.util.ArrayList;
import java.util.List;

public class WeatherRepository {

    private final CurrentWeatherDao currentWeatherDao;
    private final ForecastWeatherDao forecastWeatherDao;
    private final Context appContext;

    public WeatherRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.currentWeatherDao = new CurrentWeatherDao(appContext);
        this.forecastWeatherDao = new ForecastWeatherDao(appContext);
    }

    public void cacheCurrentWeather(CurrentWeather weather) {
        currentWeatherDao.insertOrReplace(weather);
    }

    public void cacheForecastWeather(String adCode, List<ForecastWeather> forecasts) {
        forecastWeatherDao.replaceForecasts(adCode, forecasts);
    }

    @Nullable
    public CurrentWeather getCachedCurrentWeather(String adCode) {
        return currentWeatherDao.getByAdCode(adCode);
    }

    public List<ForecastWeather> getCachedForecastWeather(String adCode) {
        return forecastWeatherDao.getByAdCode(adCode);
    }

    public CurrentWeather createMockCurrentWeather(String adCode, String cityName) {
        CurrentWeather weather = new CurrentWeather();
        weather.setAdCode(adCode);
        weather.setCityName(cityName);
        weather.setWeather(resolveMockWeather(adCode));
        weather.setTemperature(resolveMockTemperature(adCode));
        weather.setHumidity(resolveMockHumidity(adCode));
        weather.setWindDirection(resolveMockWindDirection(adCode));
        weather.setWindPower(appContext.getString(R.string.wind_power_level));
        weather.setHighTemp(resolveMockHighTemp(adCode));
        weather.setLowTemp(resolveMockLowTemp(adCode));
        weather.setReportTime(resolveMockReportTime(adCode));
        weather.setCacheTime(System.currentTimeMillis());
        return weather;
    }

    public List<ForecastWeather> createMockForecastWeather(String adCode, String cityName) {
        List<ForecastWeather> forecasts = new ArrayList<>();
        String[] dates = {"周二", "周三", "周四", "周五"};
        String[] dayWeather = {
                appContext.getString(R.string.weather_cloudy),
                appContext.getString(R.string.weather_sunny),
                appContext.getString(R.string.weather_overcast),
                appContext.getString(R.string.weather_rain)
        };
        for (int i = 0; i < dates.length; i++) {
            ForecastWeather forecast = new ForecastWeather();
            forecast.setAdCode(adCode);
            forecast.setCityName(cityName);
            forecast.setForecastDate(dates[i]);
            forecast.setWeek(String.valueOf(i + 1));
            forecast.setDayWeather(dayWeather[i]);
            forecast.setNightWeather(appContext.getString(R.string.weather_sunny));
            forecast.setDayTemp(String.valueOf(27 + i));
            forecast.setNightTemp(String.valueOf(19 + i));
            forecast.setDayWind(appContext.getString(R.string.wind_east));
            forecast.setNightWind(appContext.getString(R.string.wind_north_east));
            forecast.setDayPower(appContext.getString(R.string.wind_power_level));
            forecast.setNightPower(appContext.getString(R.string.wind_power_night));
            forecast.setCacheTime(System.currentTimeMillis());
            forecasts.add(forecast);
        }
        return forecasts;
    }

    private String resolveMockWeather(String adCode) {
        if ("310100".equals(adCode)) {
            return appContext.getString(R.string.weather_rain);
        }
        if ("510100".equals(adCode)) {
            return appContext.getString(R.string.weather_cloudy);
        }
        if ("230100".equals(adCode)) {
            return appContext.getString(R.string.weather_snow);
        }
        return appContext.getString(R.string.weather_sunny);
    }

    private String resolveMockTemperature(String adCode) {
        if ("310100".equals(adCode)) {
            return "22";
        }
        if ("510100".equals(adCode)) {
            return "24";
        }
        if ("230100".equals(adCode)) {
            return "-8";
        }
        return "26";
    }

    private String resolveMockHumidity(String adCode) {
        if ("310100".equals(adCode)) {
            return "88";
        }
        if ("510100".equals(adCode)) {
            return "72";
        }
        if ("230100".equals(adCode)) {
            return "64";
        }
        return "58";
    }

    private String resolveMockWindDirection(String adCode) {
        if ("230100".equals(adCode)) {
            return appContext.getString(R.string.wind_north_east);
        }
        return appContext.getString(R.string.wind_east_south);
    }

    private String resolveMockHighTemp(String adCode) {
        if ("310100".equals(adCode)) {
            return "24";
        }
        if ("510100".equals(adCode)) {
            return "26";
        }
        if ("230100".equals(adCode)) {
            return "-4";
        }
        return "28";
    }

    private String resolveMockLowTemp(String adCode) {
        if ("310100".equals(adCode)) {
            return "19";
        }
        if ("510100".equals(adCode)) {
            return "18";
        }
        if ("230100".equals(adCode)) {
            return "-14";
        }
        return "20";
    }

    private String resolveMockReportTime(String adCode) {
        if ("310100".equals(adCode)) {
            return "09:40";
        }
        if ("510100".equals(adCode)) {
            return "11:05";
        }
        if ("230100".equals(adCode)) {
            return "07:20";
        }
        return "10:30";
    }
}
