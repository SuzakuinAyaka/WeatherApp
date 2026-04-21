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
        weather.setWeather(appContext.getString(R.string.weather_sunny));
        weather.setTemperature("26");
        weather.setHumidity("58");
        weather.setWindDirection(appContext.getString(R.string.wind_east_south));
        weather.setWindPower(appContext.getString(R.string.wind_power_level));
        weather.setHighTemp("28");
        weather.setLowTemp("20");
        weather.setReportTime("10:30");
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
}
