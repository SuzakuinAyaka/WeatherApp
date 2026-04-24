package com.dengyy.weatherapp.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.dengyy.weatherapp.utils.DateUtils;
import com.dengyy.weatherapp.utils.NetworkUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailsOfTodayActivity extends BaseActivity {

    private static final float COLLAPSED_TITLE_THRESHOLD = 0.65f;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private WeatherRepository weatherRepository;

    private View rootView;
    private AppBarLayout appBarLayout;
    private View heroContainer;
    private MaterialToolbar toolbar;
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
    private int latestLoadToken;
    private float lastCollapseProgress;
    private String currentCityTitle = "";

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
        appBarLayout = findViewById(R.id.details_app_bar);
        heroContainer = findViewById(R.id.details_hero_container);
        toolbar = findViewById(R.id.toolbar_details);
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
        toolbar.setNavigationOnClickListener(v -> finish());
        setupCollapseBehavior();
    }

    private void setupCollapseBehavior() {
        if (appBarLayout == null) {
            return;
        }
        toolbar.setTitle("");
        appBarLayout.addOnOffsetChangedListener((appBar, verticalOffset) -> {
            int totalScrollRange = appBar.getTotalScrollRange();
            if (totalScrollRange <= 0) {
                return;
            }
            float collapseProgress = Math.min(1f, Math.abs(verticalOffset) / (float) totalScrollRange);
            lastCollapseProgress = collapseProgress;
            applyHeaderCollapseState(collapseProgress);
        });
    }

    private void applyHeaderCollapseState(float collapseProgress) {
        boolean showCollapsedTitle = collapseProgress >= COLLAPSED_TITLE_THRESHOLD;
        toolbar.setTitle(showCollapsedTitle ? currentCityTitle : "");
        if (heroContainer == null) {
            return;
        }
        float fadeProgress = Math.min(1f, collapseProgress / COLLAPSED_TITLE_THRESHOLD);
        heroContainer.setAlpha(1f - fadeProgress);
    }

    private void setupForecastList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_details_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        forecastAdapter = new DetailsForecastAdapter();
        recyclerView.setAdapter(forecastAdapter);
    }

    private void loadWeatherDetails() {
        final int loadToken = ++latestLoadToken;
        executor.execute(() -> {
            User user = userRepository.getLoginUser();
            if (user == null) {
                runOnUiThread(this::finish);
                return;
            }

            long userId = user.getId();
            cityRepository.ensureConfiguredCities(userId);
            City city = resolveTargetCity(userId, getIntent().getStringExtra("adcode"));
            if (city == null) {
                runOnUiThread(this::finish);
                return;
            }

            WeatherRepository.WeatherSnapshot cachedSnapshot = weatherRepository.getCachedSnapshot(city);
            if (loadToken == latestLoadToken) {
                runOnUiThread(() -> render(
                        city,
                        cachedSnapshot.getCurrentWeather(),
                        cachedSnapshot.getForecasts()));
            }

            if (!NetworkUtils.isConnected(this)) {
                if (loadToken == latestLoadToken) {
                    runOnUiThread(() -> {
                        if (hasUsableWeatherData(cachedSnapshot.getCurrentWeather(), cachedSnapshot.getForecasts())) {
                            Snackbar.make(rootView, R.string.message_details_offline_cache, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(rootView, R.string.message_details_empty_no_network, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                return;
            }

            WeatherRepository.WeatherSnapshot freshSnapshot = weatherRepository.getWeatherSnapshot(city, true);
            if (loadToken != latestLoadToken) {
                return;
            }
            runOnUiThread(() -> {
                render(city, freshSnapshot.getCurrentWeather(), freshSnapshot.getForecasts());
                if (!TextUtils.isEmpty(freshSnapshot.getMessage())) {
                    Snackbar.make(rootView, freshSnapshot.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean hasUsableWeatherData(
            @Nullable CurrentWeather currentWeather,
            @Nullable List<ForecastWeather> forecasts) {
        if (currentWeather != null && !TextUtils.equals("--", currentWeather.getWeather())) {
            return true;
        }
        return forecasts != null && !forecasts.isEmpty();
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
        currentCityTitle = city.getCityName();
        applyHeaderCollapseState(lastCollapseProgress);
        cityView.setText(city.getCityName());
        provinceView.setText(city.getProvince());
        weatherView.setText(currentWeather.getWeather());
        temperatureView.setText(getString(R.string.main_temperature_unit, currentWeather.getTemperature()));
        String refreshedAt = currentWeather.getCacheTime() > 0
                ? DateUtils.format(currentWeather.getCacheTime())
                : "--";
        highLowView.setText(getString(
                R.string.main_high_low,
                currentWeather.getHighTemp(),
                currentWeather.getLowTemp()
        ));
        updatedAtView.setText(getString(
                R.string.main_weather_updated_at,
                refreshedAt
        ));

        conditionValueView.setText(currentWeather.getWeather());
        humidityValueView.setText(getString(R.string.main_humidity_value, currentWeather.getHumidity()));
        windDirectionValueView.setText(currentWeather.getWindDirection());
        windPowerValueView.setText(currentWeather.getWindPower());
        provinceValueView.setText(city.getProvince());
        forecastCountView.setText(getForecastCountText(forecasts));
        forecastAdapter.submitList(forecasts);

        applyWeatherTheme(currentWeather.getWeather());
    }

    private String getForecastCountText(@Nullable List<ForecastWeather> forecasts) {
        int count = forecasts == null ? 0 : forecasts.size();
        return getString(R.string.details_forecast_count_value, count);
    }

    private void applyWeatherTheme(@Nullable String weather) {
        int[] colors = WeatherThemeResolver.resolveGradientColors(this, weather);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ColorUtils.setAlphaComponent(colors[0], 255),
                        ColorUtils.setAlphaComponent(colors[1], 255)
                }
        );
        rootView.setBackground(gradientDrawable);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().mutate().setTint(getColor(R.color.main_on_surface_strong));
        }
    }
}
