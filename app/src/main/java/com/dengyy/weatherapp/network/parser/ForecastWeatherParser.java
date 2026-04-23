package com.dengyy.weatherapp.network.parser;

import com.dengyy.weatherapp.model.ForecastWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForecastWeatherParser {

    public ParseResult parse(String adCode, String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        validateSuccess(root);
        JSONArray forecasts = root.optJSONArray("forecasts");
        if (forecasts == null || forecasts.length() == 0) {
            throw new JSONException("Missing forecasts array");
        }

        JSONObject forecastRoot = forecasts.optJSONObject(0);
        if (forecastRoot == null) {
            throw new JSONException("Missing forecast object");
        }

        String cityName = forecastRoot.optString("city");
        JSONArray casts = forecastRoot.optJSONArray("casts");
        if (casts == null || casts.length() == 0) {
            return new ParseResult(cityName, null, Collections.emptyList());
        }

        ForecastWeather todayForecast = parseCast(
                forecastRoot,
                casts.optJSONObject(0),
                adCode,
                cityName
        );

        List<ForecastWeather> futureForecasts = new ArrayList<>();
        int startIndex = casts.length() > 1 ? 1 : 0;
        for (int i = startIndex; i < casts.length() && futureForecasts.size() < 4; i++) {
            JSONObject cast = casts.optJSONObject(i);
            if (cast == null) {
                continue;
            }
            futureForecasts.add(parseCast(forecastRoot, cast, adCode, cityName));
        }

        if (futureForecasts.isEmpty() && todayForecast != null) {
            futureForecasts.add(todayForecast);
        }

        return new ParseResult(cityName, todayForecast, futureForecasts);
    }

    private void validateSuccess(JSONObject root) throws JSONException {
        String status = root.optString("status");
        if ("1".equals(status)) {
            return;
        }
        String info = root.optString("info");
        String infocode = root.optString("infocode");
        throw new JSONException(buildErrorMessage("AMap forecast failed", info, infocode));
    }

    private String buildErrorMessage(String prefix, String info, String infocode) {
        if ("10009".equals(infocode)) {
            return prefix + ": " + info + " (" + infocode + "). Please use an AMap Web Service key and rebuild the app.";
        }
        return prefix + ": " + info + " (" + infocode + ")";
    }

    private ForecastWeather parseCast(
            JSONObject forecastRoot,
            JSONObject cast,
            String adCode,
            String cityName
    ) {
        ForecastWeather forecast = new ForecastWeather();
        forecast.setAdCode(adCode);
        forecast.setCityName(cityName);
        forecast.setForecastDate(cast.optString("date"));
        forecast.setWeek(cast.optString("week"));
        forecast.setDayWeather(cast.optString("dayweather"));
        forecast.setNightWeather(cast.optString("nightweather"));
        forecast.setDayTemp(cast.optString("daytemp"));
        forecast.setNightTemp(cast.optString("nighttemp"));
        forecast.setDayWind(cast.optString("daywind"));
        forecast.setNightWind(cast.optString("nightwind"));
        forecast.setDayPower(cast.optString("daypower"));
        forecast.setNightPower(cast.optString("nightpower"));
        forecast.setCacheTime(System.currentTimeMillis());
        return forecast;
    }

    public static final class ParseResult {

        private final String cityName;
        private final ForecastWeather todayForecast;
        private final List<ForecastWeather> futureForecasts;

        public ParseResult(
                String cityName,
                ForecastWeather todayForecast,
                List<ForecastWeather> futureForecasts
        ) {
            this.cityName = cityName;
            this.todayForecast = todayForecast;
            this.futureForecasts = futureForecasts;
        }

        public String getCityName() {
            return cityName;
        }

        public ForecastWeather getTodayForecast() {
            return todayForecast;
        }

        public List<ForecastWeather> getFutureForecasts() {
            return futureForecasts;
        }
    }
}
