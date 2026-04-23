package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.adapter.CitySearchAdapter;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCityActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_CITY_NAME = "selected_city_name";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserRepository userRepository;
    private CityRepository cityRepository;

    private TextInputEditText searchInputView;
    private LinearProgressIndicator loadingIndicator;
    private TextView hintView;
    private TextView emptyView;
    private CitySearchAdapter citySearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        setupPageBehavior(R.id.add_city_root);

        userRepository = new UserRepository(this);
        cityRepository = new CityRepository(this);

        ImageButton backButton = findViewById(R.id.button_back);
        MaterialButton searchButton = findViewById(R.id.button_search_city);
        searchInputView = findViewById(R.id.input_search_city);
        loadingIndicator = findViewById(R.id.progress_search);
        hintView = findViewById(R.id.text_search_hint);
        emptyView = findViewById(R.id.text_search_empty);
        RecyclerView recyclerView = findViewById(R.id.recycler_search_result);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        citySearchAdapter = new CitySearchAdapter(this::addCity);
        recyclerView.setAdapter(citySearchAdapter);

        backButton.setOnClickListener(v -> finish());
        searchButton.setOnClickListener(v -> performSearch());
        searchInputView.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (isSearchAction) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void performSearch() {
        String keyword = searchInputView.getText() == null ? "" : searchInputView.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            searchInputView.setError(getString(R.string.error_city_keyword_required));
            return;
        }
        searchInputView.setError(null);
        setLoading(true);
        hintView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                List<City> cities = cityRepository.searchCities(keyword);
                mainHandler.post(() -> renderSearchResults(cities));
            } catch (Exception exception) {
                mainHandler.post(() -> renderSearchError(exception.getMessage()));
            }
        });
    }

    private void renderSearchResults(List<City> cities) {
        setLoading(false);
        citySearchAdapter.submitList(cities);
        emptyView.setVisibility(cities.isEmpty() ? View.VISIBLE : View.GONE);
        if (cities.isEmpty()) {
            emptyView.setText(R.string.empty_city_search_result);
        }
    }

    private void renderSearchError(String message) {
        setLoading(false);
        citySearchAdapter.submitList(Collections.emptyList());
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(R.string.empty_city_search_result);
        String safeMessage = TextUtils.isEmpty(message)
                ? getString(R.string.message_city_search_failed)
                : message;
        Snackbar.make(findViewById(R.id.add_city_root), safeMessage, Snackbar.LENGTH_LONG).show();
    }

    private void addCity(City city) {
        executor.execute(() -> {
            long userId = userRepository.getLoginUserId();
            if (userId <= 0) {
                mainHandler.post(this::finish);
                return;
            }

            CityRepository.AddCityResult result = cityRepository.addCityForUser(userId, city);
            boolean switched = false;
            if (result == CityRepository.AddCityResult.SUCCESS
                    || result == CityRepository.AddCityResult.ALREADY_EXISTS) {
                switched = cityRepository.switchCurrentCity(userId, city.getAdCode());
            }
            boolean finalSwitched = switched;
            mainHandler.post(() -> handleAddCityResult(city, result, finalSwitched));
        });
    }

    private void handleAddCityResult(City city, CityRepository.AddCityResult result, boolean switched) {
        int messageResId;
        if (result == CityRepository.AddCityResult.SUCCESS) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_CITY_NAME, city.getCityName());
            setResult(RESULT_OK, resultIntent);
            Snackbar.make(
                    findViewById(R.id.add_city_root),
                    switched
                            ? getString(R.string.message_city_switched, city.getCityName())
                            : getString(R.string.message_city_added, city.getCityName()),
                    Snackbar.LENGTH_SHORT
            ).show();
            mainHandler.postDelayed(this::finish, 180L);
            return;
        } else if (result == CityRepository.AddCityResult.ALREADY_EXISTS) {
            if (switched) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_CITY_NAME, city.getCityName());
                setResult(RESULT_OK, resultIntent);
                Snackbar.make(
                        findViewById(R.id.add_city_root),
                        getString(R.string.message_city_switched, city.getCityName()),
                        Snackbar.LENGTH_SHORT
                ).show();
                mainHandler.postDelayed(this::finish, 180L);
                return;
            }
            messageResId = R.string.message_city_exists;
        } else {
            messageResId = R.string.message_city_add_failed;
        }
        Snackbar.make(findViewById(R.id.add_city_root), messageResId, Snackbar.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
