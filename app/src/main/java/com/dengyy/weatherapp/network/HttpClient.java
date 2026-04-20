package com.dengyy.weatherapp.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public final class HttpClient {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private HttpClient() {
    }

    public static OkHttpClient getInstance() {
        return CLIENT;
    }
}
