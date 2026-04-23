package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.CityDao;
import com.dengyy.weatherapp.db.dao.UserDao;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.network.CitySearchApiService;
import com.dengyy.weatherapp.network.parser.CitySearchParser;
import com.dengyy.weatherapp.utils.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityRepository {

    public static final String DEFAULT_CITY_NAME = "西安";
    public static final String DEFAULT_CITY_AD_CODE = "610100";
    public static final String DEFAULT_CITY_PROVINCE = "陕西";

    public static final String SAMPLE_SUNNY_AD_CODE = "debug_sunny";
    public static final String SAMPLE_RAIN_AD_CODE = "debug_rain";
    public static final String SAMPLE_CLOUDY_AD_CODE = "debug_cloudy";
    public static final String SAMPLE_SNOW_AD_CODE = "debug_snow";

    public enum AddCityResult {
        SUCCESS,
        ALREADY_EXISTS,
        INVALID_INPUT
    }

    public enum DeleteCityResult {
        SUCCESS,
        LAST_CITY_BLOCKED,
        NOT_FOUND
    }

    private final Context appContext;
    private final CityDao cityDao;
    private final UserDao userDao;
    private final CitySearchApiService citySearchApiService;
    private final CitySearchParser citySearchParser;

    public CityRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.cityDao = new CityDao(appContext);
        this.userDao = new UserDao(appContext);
        this.citySearchApiService = new CitySearchApiService();
        this.citySearchParser = new CitySearchParser();
    }

    public long addCity(long userId, String cityName, String adCode, String province) {
        if (userId <= 0 || TextUtils.isEmpty(cityName) || TextUtils.isEmpty(adCode)) {
            return -1;
        }
        City city = new City();
        city.setUserId(userId);
        city.setCityName(cityName);
        city.setAdCode(adCode);
        city.setProvince(province);
        city.setCurrent(cityDao.getCurrentCity(userId) == null);
        city.setCreatedAt(System.currentTimeMillis());
        long rowId = cityDao.insert(city);
        if (rowId > 0 && city.isCurrent()) {
            userDao.updateCurrentCity(userId, adCode);
        }
        return rowId;
    }

    public AddCityResult addCityForUser(long userId, City city) {
        if (city == null) {
            return AddCityResult.INVALID_INPUT;
        }
        long rowId = addCity(userId, city.getCityName(), city.getAdCode(), city.getProvince());
        if (rowId > 0) {
            return AddCityResult.SUCCESS;
        }
        return rowId == -1 ? AddCityResult.ALREADY_EXISTS : AddCityResult.INVALID_INPUT;
    }

    public void ensureConfiguredCities(long userId) {
        if (userId <= 0) {
            return;
        }
        if (getSavedCities(userId).isEmpty()) {
            ensureDefaultCity(userId);
        }
        syncDebugSampleCities(userId, SPUtils.isDebugSampleCitiesEnabled(appContext));
    }

    private void ensureDefaultCity(long userId) {
        ensureStoredCity(userId, DEFAULT_CITY_NAME, DEFAULT_CITY_AD_CODE, DEFAULT_CITY_PROVINCE);
    }

    private void ensureStoredCity(long userId, String cityName, String adCode, String province) {
        addCity(userId, cityName, adCode, province);
        cityDao.updateCityDisplayInfo(userId, adCode, cityName, province);
    }

    public void syncDebugSampleCities(long userId, boolean enabled) {
        if (userId <= 0) {
            return;
        }
        if (enabled) {
            ensureStoredCity(userId, "北京（测试）", SAMPLE_SUNNY_AD_CODE, "北京");
            ensureStoredCity(userId, "上海（测试）", SAMPLE_RAIN_AD_CODE, "上海");
            ensureStoredCity(userId, "成都（测试）", SAMPLE_CLOUDY_AD_CODE, "四川");
            ensureStoredCity(userId, "哈尔滨（测试）", SAMPLE_SNOW_AD_CODE, "黑龙江");
            return;
        }

        City currentCity = getCurrentCity(userId);
        for (String adCode : getDebugSampleAdCodes()) {
            cityDao.deleteCity(userId, adCode);
        }
        if (currentCity != null && isDebugSampleCityAdCode(currentCity.getAdCode())) {
            List<City> remainingCities = getSavedCities(userId);
            if (!remainingCities.isEmpty()) {
                switchCurrentCity(userId, remainingCities.get(0).getAdCode());
            } else {
                ensureDefaultCity(userId);
                switchCurrentCity(userId, DEFAULT_CITY_AD_CODE);
            }
        }
    }

    public List<City> getSavedCities(long userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        return cityDao.getCitiesByUserId(userId);
    }

    public boolean switchCurrentCity(long userId, String adCode) {
        boolean success = cityDao.setCurrentCity(userId, adCode);
        if (success) {
            userDao.updateCurrentCity(userId, adCode);
        }
        return success;
    }

    public boolean deleteCity(long userId, String adCode) {
        return cityDao.deleteCity(userId, adCode);
    }

    public DeleteCityResult deleteCityForUser(long userId, String adCode) {
        List<City> savedCities = getSavedCities(userId);
        if (savedCities.size() <= 1) {
            return DeleteCityResult.LAST_CITY_BLOCKED;
        }

        City currentCity = getCurrentCity(userId);
        boolean deleted = cityDao.deleteCity(userId, adCode);
        if (!deleted) {
            return DeleteCityResult.NOT_FOUND;
        }

        if (currentCity != null && TextUtils.equals(currentCity.getAdCode(), adCode)) {
            List<City> remainingCities = getSavedCities(userId);
            if (!remainingCities.isEmpty()) {
                switchCurrentCity(userId, remainingCities.get(0).getAdCode());
            }
        }
        return DeleteCityResult.SUCCESS;
    }

    public List<City> searchCities(String keyword) throws Exception {
        if (TextUtils.isEmpty(keyword)) {
            return Collections.emptyList();
        }
        String response = citySearchApiService.searchCities(keyword.trim());
        return citySearchParser.parse(response);
    }

    @Nullable
    public City getCurrentCity(long userId) {
        return cityDao.getCurrentCity(userId);
    }

    public static boolean isDebugSampleCityAdCode(@Nullable String adCode) {
        return SAMPLE_SUNNY_AD_CODE.equals(adCode)
                || SAMPLE_RAIN_AD_CODE.equals(adCode)
                || SAMPLE_CLOUDY_AD_CODE.equals(adCode)
                || SAMPLE_SNOW_AD_CODE.equals(adCode);
    }

    public static List<String> getDebugSampleAdCodes() {
        List<String> adCodes = new ArrayList<>();
        adCodes.add(SAMPLE_SUNNY_AD_CODE);
        adCodes.add(SAMPLE_RAIN_AD_CODE);
        adCodes.add(SAMPLE_CLOUDY_AD_CODE);
        adCodes.add(SAMPLE_SNOW_AD_CODE);
        return adCodes;
    }
}
