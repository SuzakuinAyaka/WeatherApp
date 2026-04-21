package com.dengyy.weatherapp.constants;

public final class Constants {

    public static final String DB_NAME = "weather_app.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_CITIES = "cities";
    public static final String TABLE_CURRENT_WEATHER_CACHE = "current_weather_cache";
    public static final String TABLE_FORECAST_WEATHER_CACHE = "forecast_weather_cache";

    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_CURRENT_CITY_CODE = "current_city_code";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    public static final String COL_CITY_NAME = "city_name";
    public static final String COL_AD_CODE = "adcode";
    public static final String COL_PROVINCE = "province";
    public static final String COL_IS_CURRENT = "is_current";

    public static final String COL_WEATHER = "weather";
    public static final String COL_TEMPERATURE = "temperature";
    public static final String COL_HUMIDITY = "humidity";
    public static final String COL_WIND_DIRECTION = "wind_direction";
    public static final String COL_WIND_POWER = "wind_power";
    public static final String COL_HIGH_TEMP = "high_temp";
    public static final String COL_LOW_TEMP = "low_temp";
    public static final String COL_REPORT_TIME = "report_time";
    public static final String COL_CACHE_TIME = "cache_time";

    public static final String COL_FORECAST_DATE = "forecast_date";
    public static final String COL_WEEK = "week";
    public static final String COL_DAY_WEATHER = "day_weather";
    public static final String COL_NIGHT_WEATHER = "night_weather";
    public static final String COL_DAY_TEMP = "day_temp";
    public static final String COL_NIGHT_TEMP = "night_temp";
    public static final String COL_DAY_WIND = "day_wind";
    public static final String COL_NIGHT_WIND = "night_wind";
    public static final String COL_DAY_POWER = "day_power";
    public static final String COL_NIGHT_POWER = "night_power";

    public static final String SP_NAME = "weather_app_sp";
    public static final String SP_KEY_LOGIN_USER_ID = "login_user_id";
    public static final String SP_KEY_LOGIN_USERNAME = "login_username";
    public static final String SP_KEY_THEME_FOLLOW_SYSTEM = "theme_follow_system";
    public static final String SP_KEY_THEME_MODE = "theme_mode";

    private Constants() {
    }
}
