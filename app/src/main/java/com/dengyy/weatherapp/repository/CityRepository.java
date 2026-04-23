package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.CityDao;
import com.dengyy.weatherapp.db.dao.UserDao;
import com.dengyy.weatherapp.model.City;
import com.dengyy.weatherapp.network.CitySearchApiService;
import com.dengyy.weatherapp.network.parser.CitySearchParser;

import java.util.Collections;
import java.util.List;

public class CityRepository {

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

    private final CityDao cityDao;
    private final UserDao userDao;
    private final CitySearchApiService citySearchApiService;
    private final CitySearchParser citySearchParser;

    public CityRepository(Context context) {
        Context appContext = context.getApplicationContext();
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

    public void ensurePresetCities(long userId) {
        if (userId <= 0) {
            return;
        }
        ensurePresetCity(userId, "\u5317\u4eac", "110100", "\u5317\u4eac");
        ensurePresetCity(userId, "\u4e0a\u6d77", "310100", "\u4e0a\u6d77");
        ensurePresetCity(userId, "\u6210\u90fd", "510100", "\u56db\u5ddd");
        ensurePresetCity(userId, "\u54c8\u5c14\u6ee8", "230100", "\u9ed1\u9f99\u6c5f");
    }

    private void ensurePresetCity(long userId, String cityName, String adCode, String province) {
        addCity(userId, cityName, adCode, province);
        cityDao.updateCityDisplayInfo(userId, adCode, cityName, province);
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
}
