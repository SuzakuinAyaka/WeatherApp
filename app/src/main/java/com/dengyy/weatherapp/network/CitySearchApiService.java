package com.dengyy.weatherapp.network;

import androidx.annotation.NonNull;

import com.dengyy.weatherapp.config.ApiConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class CitySearchApiService {

    @NonNull
    public String searchCities(String keyword) throws IOException {
        HttpUrl baseUrl = HttpUrl.parse(ApiConfig.BASE_URL + ApiConfig.PATH_CITY_SEARCH);
        if (baseUrl == null) {
            throw new IOException("Invalid base url: " + ApiConfig.BASE_URL + ApiConfig.PATH_CITY_SEARCH);
        }

        HttpUrl url = baseUrl.newBuilder()
                .addQueryParameter("key", ApiConfig.API_KEY)
                .addQueryParameter("address", keyword)
                .addQueryParameter("output", ApiConfig.OUTPUT_JSON)
                .build();

        Request request = new Request.Builder().url(url).get().build();
        try (Response response = HttpClient.getInstance().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("City search failed with code " + response.code());
            }
            return response.body().string();
        }
    }
}
