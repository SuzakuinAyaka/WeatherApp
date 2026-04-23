package com.dengyy.weatherapp.network.parser;

import com.dengyy.weatherapp.model.CurrentWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CurrentWeatherParser {

    public CurrentWeather parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        validateSuccess(root);
        JSONArray lives = root.optJSONArray("lives");
        if (lives == null || lives.length() == 0) {
            throw new JSONException("Missing lives array");
        }

        JSONObject live = lives.optJSONObject(0);
        if (live == null) {
            throw new JSONException("Missing current weather object");
        }

        CurrentWeather weather = new CurrentWeather();
        weather.setAdCode(live.optString("adcode"));
        weather.setCityName(live.optString("city"));
        weather.setWeather(live.optString("weather"));
        weather.setTemperature(live.optString("temperature"));
        weather.setHumidity(live.optString("humidity"));
        weather.setWindDirection(live.optString("winddirection"));
        weather.setWindPower(live.optString("windpower"));
        weather.setReportTime(live.optString("reporttime"));
        weather.setCacheTime(System.currentTimeMillis());
        return weather;
    }

    private void validateSuccess(JSONObject root) throws JSONException {
        String status = root.optString("status");
        if ("1".equals(status)) {
            return;
        }
        String info = root.optString("info");
        String infocode = root.optString("infocode");
        throw new JSONException(buildErrorMessage("AMap current weather failed", info, infocode));
    }

    private String buildErrorMessage(String prefix, String info, String infocode) {
        if ("10009".equals(infocode)) {
            return prefix + ": " + info + " (" + infocode + "). Please use an AMap Web Service key and rebuild the app.";
        }
        return prefix + ": " + info + " (" + infocode + ")";
    }
}
