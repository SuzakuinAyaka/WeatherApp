package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.config.ApiConfig;
import com.dengyy.weatherapp.db.dao.CurrentWeatherDao;
import com.dengyy.weatherapp.db.dao.ForecastWeatherDao;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.ForecastWeather;
import com.dengyy.weatherapp.network.WeatherApiService;
import com.dengyy.weatherapp.network.parser.CurrentWeatherParser;
import com.dengyy.weatherapp.network.parser.ForecastWeatherParser;
import com.dengyy.weatherapp.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherRepository {

    private final CurrentWeatherDao currentWeatherDao;
    private final ForecastWeatherDao forecastWeatherDao;
    private final Context appContext;
    private final WeatherApiService weatherApiService;
    private final CurrentWeatherParser currentWeatherParser;
    private final ForecastWeatherParser forecastWeatherParser;

    public WeatherRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.currentWeatherDao = new CurrentWeatherDao(appContext);
        this.forecastWeatherDao = new ForecastWeatherDao(appContext);
        this.weatherApiService = new WeatherApiService();
        this.currentWeatherParser = new CurrentWeatherParser();
        this.forecastWeatherParser = new ForecastWeatherParser();
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

    public WeatherSnapshot getWeatherSnapshot(City city, boolean forceRefresh) {
        CurrentWeather cachedCurrent = getCachedCurrentWeather(city.getAdCode());
        List<ForecastWeather> cachedForecasts = getCachedForecastWeather(city.getAdCode());

        if (!forceRefresh && !NetworkUtils.isConnected(appContext) && cachedCurrent != null) {
            return new WeatherSnapshot(cachedCurrent, cachedForecasts, true, "Using cached weather data.");
        }

        if (TextUtils.isEmpty(ApiConfig.API_KEY)) {
            return fallbackSnapshot(city, cachedCurrent, cachedForecasts, "AMap key is empty. Rebuild the app after updating amap.properties.");
        }

        try {
            String forecastJson = weatherApiService.getForecastWeather(city.getAdCode());
            ForecastWeatherParser.ParseResult forecastResult =
                    forecastWeatherParser.parse(city.getAdCode(), forecastJson);

            String currentJson = weatherApiService.getCurrentWeather(city.getAdCode());
            CurrentWeather currentWeather = currentWeatherParser.parse(currentJson);

            enrichCurrentWeather(city, currentWeather, forecastResult.getCityName(), forecastResult.getTodayForecast());

            List<ForecastWeather> forecasts = new ArrayList<>(forecastResult.getFutureForecasts());
            cacheCurrentWeather(currentWeather);
            cacheForecastWeather(city.getAdCode(), forecasts);
            return new WeatherSnapshot(currentWeather, forecasts, false, null);
        } catch (Exception exception) {
            return fallbackSnapshot(city, cachedCurrent, cachedForecasts, exception.getMessage());
        }
    }

    private void enrichCurrentWeather(
            City city,
            CurrentWeather currentWeather,
            @Nullable String forecastCityName,
            @Nullable ForecastWeather todayForecast
    ) {
        if (TextUtils.isEmpty(currentWeather.getAdCode())) {
            currentWeather.setAdCode(city.getAdCode());
        }
        if (TextUtils.isEmpty(currentWeather.getCityName())) {
            currentWeather.setCityName(
                    !TextUtils.isEmpty(forecastCityName) ? forecastCityName : city.getCityName()
            );
        }
        if (todayForecast != null) {
            currentWeather.setHighTemp(todayForecast.getDayTemp());
            currentWeather.setLowTemp(todayForecast.getNightTemp());
        }
        currentWeather.setCacheTime(System.currentTimeMillis());
    }

    private WeatherSnapshot fallbackSnapshot(
            City city,
            @Nullable CurrentWeather cachedCurrent,
            @Nullable List<ForecastWeather> cachedForecasts,
            @Nullable String errorMessage
    ) {
        if (looksLikeLegacyMock(city.getAdCode(), cachedCurrent, cachedForecasts)) {
            cachedCurrent = null;
            cachedForecasts = null;
        }
        CurrentWeather currentWeather = cachedCurrent != null
                ? cachedCurrent
                : buildEmptyCurrentWeather(city);
        List<ForecastWeather> forecasts = cachedForecasts != null
                ? cachedForecasts
                : Collections.emptyList();
        return new WeatherSnapshot(currentWeather, forecasts, true, errorMessage);
    }

    private CurrentWeather buildEmptyCurrentWeather(City city) {
        CurrentWeather weather = new CurrentWeather();
        weather.setAdCode(city.getAdCode());
        weather.setCityName(city.getCityName());
        weather.setWeather("--");
        weather.setTemperature("--");
        weather.setHumidity("--");
        weather.setWindDirection("--");
        weather.setWindPower("--");
        weather.setHighTemp("--");
        weather.setLowTemp("--");
        weather.setReportTime("--");
        weather.setCacheTime(System.currentTimeMillis());
        return weather;
    }

    private boolean looksLikeLegacyMock(
            String adCode,
            @Nullable CurrentWeather currentWeather,
            @Nullable List<ForecastWeather> forecasts
    ) {
        if (currentWeather == null) {
            return false;
        }
        if ("110100".equals(adCode)) {
            return isSameWeather(currentWeather, "26", "58", "28", "20", "10:30");
        }
        if ("310100".equals(adCode)) {
            return isSameWeather(currentWeather, "22", "88", "24", "19", "09:40");
        }
        if ("510100".equals(adCode)) {
            return isSameWeather(currentWeather, "24", "72", "26", "18", "11:05");
        }
        if ("230100".equals(adCode)) {
            return isSameWeather(currentWeather, "-8", "64", "-4", "-14", "07:20");
        }
        return false;
    }

    private boolean isSameWeather(
            CurrentWeather currentWeather,
            String temperature,
            String humidity,
            String highTemp,
            String lowTemp,
            String reportTime
    ) {
        return TextUtils.equals(temperature, currentWeather.getTemperature())
                && TextUtils.equals(humidity, currentWeather.getHumidity())
                && TextUtils.equals(highTemp, currentWeather.getHighTemp())
                && TextUtils.equals(lowTemp, currentWeather.getLowTemp())
                && TextUtils.equals(reportTime, currentWeather.getReportTime());
    }

    public static final class WeatherSnapshot {

        private final CurrentWeather currentWeather;
        private final List<ForecastWeather> forecasts;
        private final boolean fromCache;
        @Nullable
        private final String message;

        public WeatherSnapshot(
                CurrentWeather currentWeather,
                List<ForecastWeather> forecasts,
                boolean fromCache,
                @Nullable String message
        ) {
            this.currentWeather = currentWeather;
            this.forecasts = forecasts;
            this.fromCache = fromCache;
            this.message = message;
        }

        public CurrentWeather getCurrentWeather() {
            return currentWeather;
        }

        public List<ForecastWeather> getForecasts() {
            return forecasts;
        }

        public boolean isFromCache() {
            return fromCache;
        }

        @Nullable
        public String getMessage() {
            return message;
        }
    }
}
