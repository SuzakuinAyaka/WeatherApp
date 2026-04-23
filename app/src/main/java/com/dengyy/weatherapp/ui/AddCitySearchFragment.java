package com.dengyy.weatherapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class AddCitySearchFragment extends Fragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserRepository userRepository;
    private CityRepository cityRepository;

    private View rootView;
    private TextInputEditText searchInputView;
    private LinearProgressIndicator loadingIndicator;
    private TextView searchEmptyView;
    private CitySearchAdapter citySearchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_city_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        userRepository = new UserRepository(requireContext());
        cityRepository = new CityRepository(requireContext());

        bindViews(view);
        setupActions(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void bindViews(View view) {
        searchInputView = view.findViewById(R.id.input_search_city);
        loadingIndicator = view.findViewById(R.id.progress_search);
        searchEmptyView = view.findViewById(R.id.text_search_empty);
        searchEmptyView.setVisibility(View.VISIBLE);
        searchEmptyView.setText(R.string.empty_city_search_initial);

        RecyclerView searchResultRecycler = view.findViewById(R.id.recycler_search_result);
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        citySearchAdapter = new CitySearchAdapter(this::addCity);
        searchResultRecycler.setAdapter(citySearchAdapter);
    }

    private void setupActions(View view) {
        MaterialButton searchButton = view.findViewById(R.id.button_search_city);
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

    private void performSearch() {
        String keyword = searchInputView.getText() == null ? "" : searchInputView.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            searchInputView.setError(getString(R.string.error_city_keyword_required));
            return;
        }
        hideKeyboard();
        searchInputView.setError(null);
        setLoading(true);
        searchEmptyView.setVisibility(View.GONE);

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
        searchEmptyView.setVisibility(cities.isEmpty() ? View.VISIBLE : View.GONE);
        searchEmptyView.setText(cities.isEmpty()
                ? getString(R.string.empty_city_search_result)
                : null);
    }

    private void renderSearchError(String message) {
        setLoading(false);
        citySearchAdapter.submitList(Collections.emptyList());
        searchEmptyView.setVisibility(View.VISIBLE);
        searchEmptyView.setText(R.string.empty_city_search_result);
        String safeMessage = TextUtils.isEmpty(message)
                ? getString(R.string.message_city_search_failed)
                : message;
        Snackbar.make(rootView, safeMessage, Snackbar.LENGTH_LONG).show();
    }

    private void addCity(City city) {
        executor.execute(() -> {
            long userId = userRepository.getLoginUserId();
            if (userId <= 0) {
                mainHandler.post(() -> requireActivity().finish());
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
        if ((result == CityRepository.AddCityResult.SUCCESS
                || result == CityRepository.AddCityResult.ALREADY_EXISTS) && switched) {
            AddCityActivity activity = (AddCityActivity) requireActivity();
            activity.setPendingResult(city.getCityName());
            activity.notifyCityDataChanged();
            requireActivity().finish();
            return;
        }
        if (result == CityRepository.AddCityResult.ALREADY_EXISTS) {
            Snackbar.make(rootView, R.string.message_city_exists, Snackbar.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(rootView, R.string.message_city_add_failed, Snackbar.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        if (searchInputView == null) {
            return;
        }
        searchInputView.clearFocus();
        InputMethodManager inputMethodManager =
                requireContext().getSystemService(InputMethodManager.class);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(searchInputView.getWindowToken(), 0);
        }
    }
}
