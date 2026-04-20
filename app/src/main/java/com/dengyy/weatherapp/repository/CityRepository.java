package com.dengyy.weatherapp.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.dengyy.weatherapp.db.dao.CityDao;
import com.dengyy.weatherapp.db.dao.UserDao;
import com.dengyy.weatherapp.model.City;

import java.util.Collections;
import java.util.List;

public class CityRepository {

    private final CityDao cityDao;
    private final UserDao userDao;

    public CityRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.cityDao = new CityDao(appContext);
        this.userDao = new UserDao(appContext);
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

    @Nullable
    public City getCurrentCity(long userId) {
        return cityDao.getCurrentCity(userId);
    }
}
