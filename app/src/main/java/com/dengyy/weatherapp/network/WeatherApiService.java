package com.dengyy.weatherapp.network;

import androidx.annotation.NonNull;

import com.dengyy.weatherapp.config.ApiConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApiService {

    @NonNull
    public String getCurrentWeather(String adCode) throws IOException {
        return executeGet(ApiConfig.PATH_CURRENT_WEATHER, adCode);
    }

    @NonNull
    public String getForecastWeather(String adCode) throws IOException {
        return executeGet(ApiConfig.PATH_FORECAST_WEATHER, adCode);
    }

    @NonNull
    private String executeGet(String path, String adCode) throws IOException {
        HttpUrl baseUrl = HttpUrl.parse(ApiConfig.BASE_URL + path);
        if (baseUrl == null) {
            throw new IOException("Invalid base url: " + ApiConfig.BASE_URL + path);
        }

        HttpUrl url = baseUrl.newBuilder()
                .addQueryParameter("key", ApiConfig.API_KEY)
                .addQueryParameter("adcode", adCode)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = HttpClient.getInstance().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Request failed with code " + response.code());
            }
            return response.body().string();
        }
    }
}
