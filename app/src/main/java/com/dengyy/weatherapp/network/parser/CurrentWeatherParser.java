package com.dengyy.weatherapp.network.parser;

import com.dengyy.weatherapp.model.CurrentWeather;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrentWeatherParser {

    public CurrentWeather parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        CurrentWeather weather = new CurrentWeather();
        weather.setAdCode(root.optString("adcode"));
        weather.setCityName(root.optString("city"));
        weather.setWeather(root.optString("weather"));
        weather.setTemperature(root.optString("temperature"));
        weather.setHumidity(root.optString("humidity"));
        weather.setWindDirection(root.optString("windDirection"));
        weather.setWindPower(root.optString("windPower"));
        weather.setHighTemp(root.optString("highTemp"));
        weather.setLowTemp(root.optString("lowTemp"));
        weather.setReportTime(root.optString("reportTime"));
        weather.setCacheTime(System.currentTimeMillis());
        return weather;
    }
}
