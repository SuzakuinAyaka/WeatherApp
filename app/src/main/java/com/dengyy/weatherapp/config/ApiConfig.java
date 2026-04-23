package com.dengyy.weatherapp.config;

import com.dengyy.weatherapp.BuildConfig;

public final class ApiConfig {

    public static final String BASE_URL = BuildConfig.AMAP_WEATHER_BASE_URL;
    public static final String API_KEY = BuildConfig.AMAP_WEB_SERVICE_KEY;
    public static final String PATH_CURRENT_WEATHER = BuildConfig.AMAP_WEATHER_PATH;
    public static final String PATH_FORECAST_WEATHER = BuildConfig.AMAP_WEATHER_PATH;
    public static final String EXTENSIONS_BASE = "base";
    public static final String EXTENSIONS_ALL = "all";
    public static final String OUTPUT_JSON = "JSON";

    private ApiConfig() {
    }
}
