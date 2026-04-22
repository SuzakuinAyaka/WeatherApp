package com.dengyy.weatherapp.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.adapter.DetailsForecastAdapter;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.ForecastWeather;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.repository.WeatherRepository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailsOfTodayActivity extends BaseActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private WeatherRepository weatherRepository;

    private View rootView;
    private TextView cityView;
    private TextView provinceView;
    private TextView weatherView;
    private TextView temperatureView;
    private TextView highLowView;
    private TextView updatedAtView;
    private TextView conditionValueView;
    private TextView humidityValueView;
    private TextView windDirectionValueView;
    private TextView windPowerValueView;
    private TextView provinceValueView;
    private TextView forecastCountView;
    private DetailsForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_of_today);
        setupPageBehavior(R.id.details_root);

        userRepository = new UserRepository(this);
        cityRepository = new CityRepository(this);
        weatherRepository = new WeatherRepository(this);

        bindViews();
        setupActions();
        setupForecastList();
        loadWeatherDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void bindViews() {
        rootView = findViewById(R.id.details_root);
        cityView = findViewById(R.id.text_details_city);
        provinceView = findViewById(R.id.text_details_province);
        weatherView = findViewById(R.id.text_details_weather);
        temperatureView = findViewById(R.id.text_details_temperature);
        highLowView = findViewById(R.id.text_details_high_low);
        updatedAtView = findViewById(R.id.text_details_updated_at);
        conditionValueView = findViewById(R.id.text_details_condition_value);
        humidityValueView = findViewById(R.id.text_details_humidity_value);
        windDirectionValueView = findViewById(R.id.text_details_wind_direction_value);
        windPowerValueView = findViewById(R.id.text_details_wind_power_value);
        provinceValueView = findViewById(R.id.text_details_province_value);
        forecastCountView = findViewById(R.id.text_details_forecast_count);
    }

    private void setupActions() {
        ImageButton backButton = findViewById(R.id.button_details_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupForecastList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_details_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        forecastAdapter = new DetailsForecastAdapter();
        recyclerView.setAdapter(forecastAdapter);
    }

    private void loadWeatherDetails() {
        executor.execute(() -> {
            User user = userRepository.getLoginUser();
            if (user == null) {
                runOnUiThread(this::finish);
                return;
            }

            long userId = user.getId();
            cityRepository.ensurePresetCities(userId);
            City city = resolveTargetCity(userId, getIntent().getStringExtra("adcode"));
            if (city == null) {
                runOnUiThread(this::finish);
                return;
            }

            CurrentWeather currentWeather = weatherRepository.getCachedCurrentWeather(city.getAdCode());
            if (currentWeather == null) {
                currentWeather = weatherRepository.createMockCurrentWeather(city.getAdCode(), city.getCityName());
                weatherRepository.cacheCurrentWeather(currentWeather);
            }

            List<ForecastWeather> forecasts = weatherRepository.getCachedForecastWeather(city.getAdCode());
            if (forecasts == null || forecasts.isEmpty()) {
                forecasts = weatherRepository.createMockForecastWeather(city.getAdCode(), city.getCityName());
                weatherRepository.cacheForecastWeather(city.getAdCode(), forecasts);
            }

            CurrentWeather finalCurrentWeather = currentWeather;
            List<ForecastWeather> finalForecasts = forecasts;
            runOnUiThread(() -> render(city, finalCurrentWeather, finalForecasts));
        });
    }

    @Nullable
    private City resolveTargetCity(long userId, @Nullable String adCode) {
        City currentCity = cityRepository.getCurrentCity(userId);
        if (TextUtils.isEmpty(adCode)) {
            return currentCity;
        }
        List<City> savedCities = cityRepository.getSavedCities(userId);
        for (City city : savedCities) {
            if (TextUtils.equals(adCode, city.getAdCode())) {
                return city;
            }
        }
        return currentCity;
    }

    private void render(City city, CurrentWeather currentWeather, List<ForecastWeather> forecasts) {
        cityView.setText(city.getCityName());
        provinceView.setText(city.getProvince());
        weatherView.setText(currentWeather.getWeather());
        temperatureView.setText(getString(R.string.main_temperature_unit, currentWeather.getTemperature()));
        highLowView.setText(getString(
                R.string.main_high_low,
                currentWeather.getHighTemp(),
                currentWeather.getLowTemp()
        ));
        updatedAtView.setText(getString(
                R.string.main_weather_updated_at,
                currentWeather.getReportTime()
        ));

        conditionValueView.setText(currentWeather.getWeather());
        humidityValueView.setText(getString(R.string.main_humidity_value, currentWeather.getHumidity()));
        windDirectionValueView.setText(currentWeather.getWindDirection());
        windPowerValueView.setText(currentWeather.getWindPower());
        provinceValueView.setText(city.getProvince());
        forecastCountView.setText(getForecastCountText(forecasts));
        forecastAdapter.submitList(forecasts == null ? Collections.emptyList() : forecasts);

        applyWeatherTheme(currentWeather.getWeather());
    }

    private String getForecastCountText(@Nullable List<ForecastWeather> forecasts) {
        int count = forecasts == null ? 0 : forecasts.size();
        return count + " day outlook";
    }

    private void applyWeatherTheme(@Nullable String weather) {
        int[] colors = resolveGradientColors(weather);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ColorUtils.setAlphaComponent(colors[0], 255),
                        ColorUtils.setAlphaComponent(colors[1], 255)
                }
        );
        rootView.setBackground(gradientDrawable);
    }

    private int[] resolveGradientColors(@Nullable String weather) {
        if (weather == null) {
            return new int[]{
                    getColor(R.color.main_bg_sunny_top),
                    getColor(R.color.main_bg_sunny_bottom)
            };
        }
        if (weather.contains(getString(R.string.weather_rain))) {
            return new int[]{
                    getColor(R.color.main_bg_rain_top),
                    getColor(R.color.main_bg_rain_bottom)
            };
        }
        if (weather.contains(getString(R.string.weather_snow))) {
            return new int[]{
                    getColor(R.color.main_bg_snow_top),
                    getColor(R.color.main_bg_snow_bottom)
            };
        }
        if (weather.contains(getString(R.string.weather_overcast))
                || weather.contains(getString(R.string.weather_cloudy))) {
            return new int[]{
                    getColor(R.color.main_bg_cloudy_top),
                    getColor(R.color.main_bg_cloudy_bottom)
            };
        }
        return new int[]{
                getColor(R.color.main_bg_sunny_top),
                getColor(R.color.main_bg_sunny_bottom)
        };
    }
}
