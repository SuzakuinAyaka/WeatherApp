package com.dengyy.weatherapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.adapter.SavedCityAdapter;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.CurrentWeather;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.dengyy.weatherapp.repository.WeatherRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageCitiesFragment extends Fragment implements AddCityActivity.OnCityDataChangedListener {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private WeatherRepository weatherRepository;

    private View rootView;
    private TextView savedCityCountView;
    private TextView savedEmptyView;
    private SavedCityAdapter savedCityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_cities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        userRepository = new UserRepository(requireContext());
        cityRepository = new CityRepository(requireContext());
        weatherRepository = new WeatherRepository(requireContext());

        savedCityCountView = view.findViewById(R.id.text_saved_city_count);
        savedEmptyView = view.findViewById(R.id.text_saved_empty);

        RecyclerView savedCitiesRecycler = view.findViewById(R.id.recycler_saved_cities);
        savedCitiesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        savedCityAdapter = new SavedCityAdapter(this::switchCity, this::confirmDeleteCity);
        savedCitiesRecycler.setAdapter(savedCityAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AddCityActivity) requireActivity()).registerCityDataChangedListener(this);
        loadManagedCities(null);
    }

    @Override
    public void onPause() {
        ((AddCityActivity) requireActivity()).unregisterCityDataChangedListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    public void onCityDataChanged() {
        loadManagedCities(null);
    }

    private void loadManagedCities(@Nullable String snackbarMessage) {
        executor.execute(() -> {
            User user = userRepository.getLoginUser();
            if (user == null) {
                mainHandler.post(() -> requireActivity().finish());
                return;
            }

            long userId = user.getId();
            cityRepository.ensureConfiguredCities(userId);
            List<City> savedCities = cityRepository.getSavedCities(userId);
            Map<String, String> temperatureMap = buildSavedCityTemperatureMap(savedCities);
            mainHandler.post(() -> renderManagedCities(savedCities, temperatureMap, snackbarMessage));
        });
    }

    private Map<String, String> buildSavedCityTemperatureMap(List<City> savedCities) {
        Map<String, String> temperaturesByAdCode = new HashMap<>();
        if (savedCities == null) {
            return temperaturesByAdCode;
        }

        for (City city : savedCities) {
            if (city == null || TextUtils.isEmpty(city.getAdCode())) {
                continue;
            }
            CurrentWeather cachedWeather = weatherRepository.getCachedCurrentWeather(city.getAdCode());
            if (cachedWeather == null
                    || TextUtils.isEmpty(cachedWeather.getHighTemp())
                    || TextUtils.isEmpty(cachedWeather.getLowTemp())) {
                continue;
            }
            temperaturesByAdCode.put(
                    city.getAdCode(),
                    getString(R.string.main_saved_city_temp_range,
                            cachedWeather.getHighTemp(),
                            cachedWeather.getLowTemp()));
        }
        return temperaturesByAdCode;
    }

    private void renderManagedCities(
            List<City> savedCities,
            Map<String, String> temperatureMap,
            @Nullable String snackbarMessage
    ) {
        savedCityCountView.setText(getString(
                R.string.add_city_saved_section_count,
                savedCities.size()));
        savedEmptyView.setVisibility(savedCities.isEmpty() ? View.VISIBLE : View.GONE);
        savedCityAdapter.submitData(savedCities, temperatureMap);

        if (!TextUtils.isEmpty(snackbarMessage)) {
            Snackbar.make(rootView, snackbarMessage, Snackbar.LENGTH_LONG).show();
        }
    }

    private void switchCity(City city) {
        if (city == null) {
            return;
        }
        executor.execute(() -> {
            long userId = userRepository.getLoginUserId();
            if (userId <= 0) {
                mainHandler.post(() -> requireActivity().finish());
                return;
            }
            boolean switched = cityRepository.switchCurrentCity(userId, city.getAdCode());
            mainHandler.post(() -> handleSwitchCityResult(city, switched));
        });
    }

    private void handleSwitchCityResult(City city, boolean switched) {
        if (!switched) {
            Snackbar.make(rootView, R.string.message_city_switch_failed, Snackbar.LENGTH_LONG).show();
            return;
        }
        AddCityActivity activity = (AddCityActivity) requireActivity();
        activity.setPendingResult(city.getCityName());
        requireActivity().finish();
    }

    private void confirmDeleteCity(City city) {
        if (city == null) {
            return;
        }
        int messageResId = city.isCurrent()
                ? R.string.dialog_delete_current_city_message
                : R.string.dialog_delete_city_message;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_delete_city_title, city.getCityName()))
                .setMessage(messageResId)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> deleteCity(city))
                .show();
    }

    private void deleteCity(City city) {
        executor.execute(() -> {
            long userId = userRepository.getLoginUserId();
            if (userId <= 0) {
                mainHandler.post(() -> requireActivity().finish());
                return;
            }

            CityRepository.DeleteCityResult result = cityRepository.deleteCityForUser(userId, city.getAdCode());
            City nextCurrentCity = cityRepository.getCurrentCity(userId);
            mainHandler.post(() -> handleDeleteCityResult(city, result, nextCurrentCity));
        });
    }

    private void handleDeleteCityResult(
            City deletedCity,
            CityRepository.DeleteCityResult result,
            @Nullable City nextCurrentCity
    ) {
        if (result == CityRepository.DeleteCityResult.LAST_CITY_BLOCKED) {
            Snackbar.make(rootView, R.string.message_city_delete_blocked_last, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (result == CityRepository.DeleteCityResult.NOT_FOUND) {
            Snackbar.make(rootView, R.string.message_city_delete_failed, Snackbar.LENGTH_LONG).show();
            return;
        }

        AddCityActivity activity = (AddCityActivity) requireActivity();
        activity.setPendingResult(nextCurrentCity != null
                ? nextCurrentCity.getCityName()
                : activity.getPendingResultCityName());
        activity.notifyCityDataChanged();

        String message = deletedCity.isCurrent() && nextCurrentCity != null
                ? getString(R.string.message_city_deleted_and_switched,
                deletedCity.getCityName(),
                nextCurrentCity.getCityName())
                : getString(R.string.message_city_deleted, deletedCity.getCityName());
        loadManagedCities(message);
    }
}
