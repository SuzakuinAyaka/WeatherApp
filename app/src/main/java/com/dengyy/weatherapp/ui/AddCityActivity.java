package com.dengyy.weatherapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.model.User;
import com.dengyy.weatherapp.repository.CityRepository;
import com.dengyy.weatherapp.repository.UserRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AddCityActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_CITY_NAME = "selected_city_name";

    private final List<OnCityDataChangedListener> cityDataChangedListeners = new ArrayList<>();

    private UserRepository userRepository;
    private CityRepository cityRepository;
    private TextView manageSummaryView;

    @Nullable
    private String pendingResultCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        setupPageBehavior(R.id.add_city_root);

        userRepository = new UserRepository(this);
        cityRepository = new CityRepository(this);

        bindViews();
        setupPager();
        refreshSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSummary();
    }

    private void bindViews() {
        manageSummaryView = findViewById(R.id.text_manage_summary);
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupPager() {
        TabLayout tabLayout = findViewById(R.id.tab_layout_add_city);
        ViewPager2 viewPager = findViewById(R.id.view_pager_add_city);
        viewPager.setAdapter(new AddCityPagerAdapter(this));
        viewPager.setCurrentItem(0, false);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.add_city_search_tab_title);
            } else {
                tab.setText(R.string.add_city_manage_tab_title);
            }
        }).attach();
    }

    void refreshSummary() {
        User user = userRepository.getLoginUser();
        if (user == null) {
            finish();
            return;
        }

        long userId = user.getId();
        cityRepository.ensureConfiguredCities(userId);
        City currentCity = cityRepository.getCurrentCity(userId);
        List<City> savedCities = cityRepository.getSavedCities(userId);
        String currentCityName = currentCity != null
                ? currentCity.getCityName()
                : getString(R.string.main_default_city);
        manageSummaryView.setText(getString(
                R.string.add_city_manage_summary,
                currentCityName,
                savedCities.size()));
    }

    void setPendingResult(@Nullable String cityName) {
        pendingResultCityName = cityName;
        Intent resultIntent = new Intent();
        if (cityName != null && !cityName.trim().isEmpty()) {
            resultIntent.putExtra(EXTRA_SELECTED_CITY_NAME, cityName);
        }
        setResult(RESULT_OK, resultIntent);
    }

    void notifyCityDataChanged() {
        refreshSummary();
        List<OnCityDataChangedListener> snapshot = new ArrayList<>(cityDataChangedListeners);
        for (OnCityDataChangedListener listener : snapshot) {
            listener.onCityDataChanged();
        }
    }

    void registerCityDataChangedListener(@NonNull OnCityDataChangedListener listener) {
        if (!cityDataChangedListeners.contains(listener)) {
            cityDataChangedListeners.add(listener);
        }
    }

    void unregisterCityDataChangedListener(@NonNull OnCityDataChangedListener listener) {
        cityDataChangedListeners.remove(listener);
    }

    @Nullable
    String getPendingResultCityName() {
        return pendingResultCityName;
    }

    private static final class AddCityPagerAdapter extends FragmentStateAdapter {

        private AddCityPagerAdapter(@NonNull AddCityActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new AddCitySearchFragment();
            }
            return new ManageCitiesFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    interface OnCityDataChangedListener {
        void onCityDataChanged();
    }
}
