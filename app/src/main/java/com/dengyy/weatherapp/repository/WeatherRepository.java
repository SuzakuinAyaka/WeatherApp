package com.dengyy.weatherapp.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.CurrentWeatherDao;
import com.dengyy.weatherapp.db.dao.ForecastWeatherDao;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.ForecastWeather;

import java.util.ArrayList;
import java.util.List;

public class WeatherRepository {

    private final CurrentWeatherDao currentWeatherDao;
    private final ForecastWeatherDao forecastWeatherDao;

    public WeatherRepository(Context context) {
        Context appContext = context.getApplicationContext();
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
        weather.setWeather("晴");
        weather.setTemperature("26");
        weather.setHumidity("58");
        weather.setWindDirection("东南风");
        weather.setWindPower("3级");
        weather.setHighTemp("28");
        weather.setLowTemp("20");
        weather.setReportTime("骨架阶段");
        weather.setCacheTime(System.currentTimeMillis());
        return weather;
    }

    public List<ForecastWeather> createMockForecastWeather(String adCode, String cityName) {
        List<ForecastWeather> forecasts = new ArrayList<>();
        String[] dates = {"周一", "周二", "周三", "周四"};
        for (int i = 0; i < dates.length; i++) {
            ForecastWeather forecast = new ForecastWeather();
            forecast.setAdCode(adCode);
            forecast.setCityName(cityName);
            forecast.setForecastDate(dates[i]);
            forecast.setWeek(String.valueOf(i + 1));
            forecast.setDayWeather("多云");
            forecast.setNightWeather("晴");
            forecast.setDayTemp(String.valueOf(27 + i));
            forecast.setNightTemp(String.valueOf(19 + i));
            forecast.setDayWind("东风");
            forecast.setNightWind("东北风");
            forecast.setDayPower("3级");
            forecast.setNightPower("2级");
            forecast.setCacheTime(System.currentTimeMillis());
            forecasts.add(forecast);
        }
        return forecasts;
    }
}
