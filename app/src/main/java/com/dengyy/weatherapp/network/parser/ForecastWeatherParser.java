package com.dengyy.weatherapp.network.parser;

import com.dengyy.weatherapp.model.ForecastWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForecastWeatherParser {

    public List<ForecastWeather> parse(String adCode, String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray array = root.optJSONArray("forecasts");
        List<ForecastWeather> forecasts = new ArrayList<>();
        if (array == null) {
            return forecasts;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            ForecastWeather forecast = new ForecastWeather();
            forecast.setAdCode(adCode);
            forecast.setCityName(item.optString("city"));
            forecast.setForecastDate(item.optString("date"));
            forecast.setWeek(item.optString("week"));
            forecast.setDayWeather(item.optString("dayWeather"));
            forecast.setNightWeather(item.optString("nightWeather"));
            forecast.setDayTemp(item.optString("dayTemp"));
            forecast.setNightTemp(item.optString("nightTemp"));
            forecast.setDayWind(item.optString("dayWind"));
            forecast.setNightWind(item.optString("nightWind"));
            forecast.setDayPower(item.optString("dayPower"));
            forecast.setNightPower(item.optString("nightPower"));
            forecast.setCacheTime(System.currentTimeMillis());
            forecasts.add(forecast);
        }
        return forecasts;
    }
}
