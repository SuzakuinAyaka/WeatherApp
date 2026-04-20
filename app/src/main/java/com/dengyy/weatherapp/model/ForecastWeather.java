package com.dengyy.weatherapp.model;

public class ForecastWeather {

    private long id;
    private String adCode;
    private String cityName;
    private String forecastDate;
    private String week;
    private String dayWeather;
    private String nightWeather;
    private String dayTemp;
    private String nightTemp;
    private String dayWind;
    private String nightWind;
    private String dayPower;
    private String nightPower;
    private long cacheTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(String forecastDate) {
        this.forecastDate = forecastDate;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDayWeather() {
        return dayWeather;
    }

    public void setDayWeather(String dayWeather) {
        this.dayWeather = dayWeather;
    }

    public String getNightWeather() {
        return nightWeather;
    }

    public void setNightWeather(String nightWeather) {
        this.nightWeather = nightWeather;
    }

    public String getDayTemp() {
        return dayTemp;
    }

    public void setDayTemp(String dayTemp) {
        this.dayTemp = dayTemp;
    }

    public String getNightTemp() {
        return nightTemp;
    }

    public void setNightTemp(String nightTemp) {
        this.nightTemp = nightTemp;
    }

    public String getDayWind() {
        return dayWind;
    }

    public void setDayWind(String dayWind) {
        this.dayWind = dayWind;
    }

    public String getNightWind() {
        return nightWind;
    }

    public void setNightWind(String nightWind) {
        this.nightWind = nightWind;
    }

    public String getDayPower() {
        return dayPower;
    }

    public void setDayPower(String dayPower) {
        this.dayPower = dayPower;
    }

    public String getNightPower() {
        return nightPower;
    }

    public void setNightPower(String nightPower) {
        this.nightPower = nightPower;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }
}
