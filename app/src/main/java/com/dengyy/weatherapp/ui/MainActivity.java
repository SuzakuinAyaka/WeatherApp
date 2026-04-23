package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.adapter.ForecastAdapter;
import com.dengyy.weatherapp.adapter.SavedCityAdapter;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.ForecastWeather;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.repository.WeatherRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private WeatherRepository weatherRepository;

    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View drawerContainer;
    private WeatherBackgroundView weatherBackgroundView;
    private TextView welcomeView;
    private TextView toolbarCityView;
    private TextView toolbarSummaryView;
    private TextView cityView;
    private TextView temperatureView;
    private TextView weatherView;
    private TextView highLowView;
    private TextView reportTimeView;
    private TextView humidityView;
    private TextView windDirectionView;
    private TextView windPowerView;
    private TextView drawerUsernameView;
    private TextView drawerCityView;
    private TextView drawerStatusView;
    private TextView savedCityCountView;
    private TextView emptyCitiesView;
    private ForecastAdapter forecastAdapter;
    private SavedCityAdapter savedCityAdapter;

    @Nullable
    private City currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPageBehavior(R.id.main_root);

        userRepository = new UserRepository(this);
        cityRepository = new CityRepository(this);
        weatherRepository = new WeatherRepository(this);

        bindViews();
        setupLists();
        setupActions();
        setupBackBehavior();
        loadPageData(false, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPageData(false, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.main_root);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        drawerContainer = findViewById(R.id.drawer_container);
        weatherBackgroundView = findViewById(R.id.view_weather_background);
        welcomeView = findViewById(R.id.text_welcome);
        toolbarCityView = findViewById(R.id.text_toolbar_city);
        toolbarSummaryView = findViewById(R.id.text_toolbar_summary);
        cityView = findViewById(R.id.text_current_city);
        temperatureView = findViewById(R.id.text_temperature);
        weatherView = findViewById(R.id.text_weather_placeholder);
        highLowView = findViewById(R.id.text_high_low);
        reportTimeView = findViewById(R.id.text_report_time);
        humidityView = findViewById(R.id.text_humidity);
        windDirectionView = findViewById(R.id.text_wind_direction);
        windPowerView = findViewById(R.id.text_wind_power);
        drawerUsernameView = findViewById(R.id.text_drawer_username);
        drawerCityView = findViewById(R.id.text_drawer_city);
        drawerStatusView = findViewById(R.id.text_drawer_status);
        savedCityCountView = findViewById(R.id.text_saved_city_count);
        emptyCitiesView = findViewById(R.id.text_empty_saved_cities);
    }

    private void setupLists() {
        RecyclerView forecastRecycler = findViewById(R.id.recycler_forecast);
        forecastRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        forecastAdapter = new ForecastAdapter();
        forecastRecycler.setAdapter(forecastAdapter);

        RecyclerView savedCitiesRecycler = findViewById(R.id.recycler_saved_cities);
        savedCitiesRecycler.setLayoutManager(new LinearLayoutManager(this));
        savedCityAdapter = new SavedCityAdapter(this::switchCity);
        savedCitiesRecycler.setAdapter(savedCityAdapter);
    }

    private void setupActions() {
        ImageButton openDrawerButton = findViewById(R.id.button_open_drawer);
        ImageButton refreshButton = findViewById(R.id.button_refresh);
        ImageButton settingsButton = findViewById(R.id.button_settings);
        ExtendedFloatingActionButton addCityButton = findViewById(R.id.button_add_city);
        MaterialButton detailsButton = findViewById(R.id.button_details);

        swipeRefreshLayout.setColorSchemeResources(R.color.main_accent);
        swipeRefreshLayout.setOnRefreshListener(() ->
                loadPageData(true, getString(R.string.message_main_refreshed))
        );

        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        refreshButton.setOnClickListener(v -> {
            swipeRefreshLayout.setRefreshing(true);
            loadPageData(true, getString(R.string.message_main_refreshed));
        });
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
        addCityButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddCityActivity.class))
        );
        detailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailsOfTodayActivity.class);
            if (currentCity != null) {
                intent.putExtra("adcode", currentCity.getAdCode());
            }
            startActivity(intent);
        });
    }

    private void setupBackBehavior() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void loadPageData(boolean forceRefresh, @Nullable String snackbarMessage) {
        executor.execute(() -> {
            User user = userRepository.getLoginUser();
            if (user == null) {
                mainHandler.post(this::navigateToLogin);
                return;
            }

            City current = ensureCurrentCity(user.getId());
            List<City> savedCities = cityRepository.getSavedCities(user.getId());
            WeatherRepository.WeatherSnapshot snapshot =
                    weatherRepository.getWeatherSnapshot(current, forceRefresh);
            CurrentWeather currentWeather = snapshot.getCurrentWeather();
            List<ForecastWeather> forecasts = snapshot.getForecasts();

            String message = snackbarMessage;
            if ((message == null || message.isEmpty()) && snapshot.getMessage() != null && !snapshot.getMessage().isEmpty()) {
                message = snapshot.getMessage();
            }
            MainUiState state = new MainUiState(user, current, savedCities, currentWeather, forecasts, message);
            mainHandler.post(() -> renderState(state));
        });
    }

    private City ensureCurrentCity(long userId) {
        cityRepository.ensurePresetCities(userId);
        City current = cityRepository.getCurrentCity(userId);
        if (current != null) {
            return current;
        }
        cityRepository.switchCurrentCity(userId, "110100");
        return cityRepository.getCurrentCity(userId);
    }

    private void renderState(MainUiState state) {
        currentCity = state.currentCity;
        swipeRefreshLayout.setRefreshing(false);

        String username = state.user.getUsername();
        String cityName = state.currentCity.getCityName();
        String weatherText = state.currentWeather.getWeather();
        String temperature = getString(R.string.main_temperature_unit, state.currentWeather.getTemperature());

        welcomeView.setText(getString(R.string.main_welcome_back, username));
        toolbarCityView.setText(cityName);
        toolbarSummaryView.setText(weatherText + " / " + temperature);
        cityView.setText(cityName);
        temperatureView.setText(temperature);
        weatherView.setText(weatherText);
        highLowView.setText(getString(
                R.string.main_high_low,
                state.currentWeather.getHighTemp(),
                state.currentWeather.getLowTemp()
        ));
        reportTimeView.setText(getString(
                R.string.main_weather_updated_at,
                state.currentWeather.getReportTime()
        ));
        humidityView.setText(getString(R.string.main_humidity_value, state.currentWeather.getHumidity()));
        windDirectionView.setText(state.currentWeather.getWindDirection());
        windPowerView.setText(state.currentWeather.getWindPower());

        drawerUsernameView.setText(username);
        drawerCityView.setText(getString(R.string.settings_current_city_label) + " / " + cityName);
        drawerStatusView.setText(getString(R.string.main_sidebar_status));
        savedCityCountView.setText(getString(
                R.string.main_drawer_city_count,
                state.savedCities.size()
        ));
        emptyCitiesView.setVisibility(state.savedCities.isEmpty() ? View.VISIBLE : View.GONE);

        forecastAdapter.submitList(state.forecasts);
        savedCityAdapter.submitList(state.savedCities);
        applyWeatherTheme(weatherText);

        if (state.message != null && !state.message.isEmpty()) {
            Snackbar.make(drawerLayout, state.message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void switchCity(City city) {
        if (city == null) {
            return;
        }
        executor.execute(() -> {
            long userId = userRepository.getLoginUserId();
            if (userId <= 0) {
                mainHandler.post(this::navigateToLogin);
                return;
            }
            cityRepository.switchCurrentCity(userId, city.getAdCode());
            mainHandler.post(() -> drawerLayout.closeDrawer(GravityCompat.START));
            loadPageData(false, getString(R.string.message_city_switched, city.getCityName()));
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void applyWeatherTheme(String weather) {
        int[] colors = resolveGradientColors(weather);
        weatherBackgroundView.setWeatherType(resolveWeatherType(weather));

        int drawerColor = ColorUtils.blendARGB(colors[0], colors[1], 0.55f);
        android.graphics.drawable.GradientDrawable drawerBackground = new android.graphics.drawable.GradientDrawable();
        drawerBackground.setColor(ColorUtils.setAlphaComponent(drawerColor, 242));
        drawerContainer.setBackground(drawerBackground);
    }

    private int resolveWeatherType(String weather) {
        if (weather == null) {
            return WeatherBackgroundView.WEATHER_SUNNY;
        }
        if (weather.contains(getString(R.string.weather_rain))) {
            return WeatherBackgroundView.WEATHER_RAIN;
        }
        if (weather.contains(getString(R.string.weather_snow))) {
            return WeatherBackgroundView.WEATHER_SNOW;
        }
        if (weather.contains(getString(R.string.weather_overcast))
                || weather.contains(getString(R.string.weather_cloudy))) {
            return WeatherBackgroundView.WEATHER_CLOUDY;
        }
        return WeatherBackgroundView.WEATHER_SUNNY;
    }

    private int[] resolveGradientColors(String weather) {
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

    private static final class MainUiState {

        private final User user;
        private final City currentCity;
        private final List<City> savedCities;
        private final CurrentWeather currentWeather;
        private final List<ForecastWeather> forecasts;
        @Nullable
        private final String message;

        private MainUiState(
                User user,
                City currentCity,
                List<City> savedCities,
                CurrentWeather currentWeather,
                List<ForecastWeather> forecasts,
                @Nullable String message
        ) {
            this.user = user;
            this.currentCity = currentCity;
            this.savedCities = savedCities;
            this.currentWeather = currentWeather;
            this.forecasts = forecasts;
            this.message = message;
        }
    }
}
