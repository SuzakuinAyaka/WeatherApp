package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.adapter.ForecastAdapter;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.repository.WeatherRepository;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserRepository userRepository = new UserRepository(this);
        CityRepository cityRepository = new CityRepository(this);
        WeatherRepository weatherRepository = new WeatherRepository(this);

        TextView welcomeView = findViewById(R.id.text_welcome);
        TextView cityView = findViewById(R.id.text_current_city);
        TextView weatherView = findViewById(R.id.text_weather_placeholder);
        Button addCityButton = findViewById(R.id.button_add_city);
        Button settingsButton = findViewById(R.id.button_settings);
        Button detailsButton = findViewById(R.id.button_details);
        RecyclerView recyclerView = findViewById(R.id.recycler_forecast);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ForecastAdapter adapter = new ForecastAdapter();
        recyclerView.setAdapter(adapter);

        User loginUser = userRepository.getLoginUser();
        String username = loginUser != null ? loginUser.getUsername() : "游客";
        welcomeView.setText("欢迎你，" + username);

        long userId = userRepository.getLoginUserId();
        City currentCity = cityRepository.getCurrentCity(userId);
        if (currentCity == null) {
            cityRepository.addCity(userId, "西安", "610100", "陕西");
            currentCity = cityRepository.getCurrentCity(userId);
        }

        if (currentCity != null) {
            cityView.setText(currentCity.getCityName());
            CurrentWeather currentWeather = weatherRepository.createMockCurrentWeather(
                    currentCity.getAdCode(),
                    currentCity.getCityName()
            );
            weatherView.setText(currentWeather.getWeather() + "  " + currentWeather.getTemperature() + "°");
            adapter.submitList(weatherRepository.createMockForecastWeather(
                    currentCity.getAdCode(),
                    currentCity.getCityName()
            ));
        }

        addCityButton.setOnClickListener(v -> startActivity(new Intent(this, AddCityActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        detailsButton.setOnClickListener(v -> startActivity(new Intent(this, DetailsOfTodayActivity.class)));
    }
}
