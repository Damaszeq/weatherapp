package com.tomaszwejner.weatherapp;

import java.util.List;

public class WeatherData {
    private final double temperature;
    private final String description;

    private String cityName;
    private String startDate;
    private String endDate;

    private final List<String> forecastDates;
    private final List<Double> forecastTemperatures;
    private final List<Double> forecastRains;
    private final List<Double> forecastWinds;
    private final List<Double> forecastPressures;  // nowe pole: ciśnienie

    public WeatherData(double temperature, String description,
                       List<String> forecastDates,
                       List<Double> forecastTemperatures,
                       List<Double> forecastRains,
                       List<Double> forecastWinds,
                       List<Double> forecastPressures) {  // rozszerzony konstruktor
        this.temperature = temperature;
        this.description = description;
        this.forecastDates = forecastDates;
        this.forecastTemperatures = forecastTemperatures;
        this.forecastRains = forecastRains;
        this.forecastWinds = forecastWinds;
        this.forecastPressures = forecastPressures;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<String> getForecastDates() {
        return forecastDates;
    }

    public List<Double> getForecastTemperatures() {
        return forecastTemperatures;
    }

    public List<Double> getForecastRains() {
        return forecastRains;
    }

    public List<Double> getForecastWinds() {
        return forecastWinds;
    }

    public List<Double> getForecastPressures() {  // getter dla ciśnienia
        return forecastPressures;
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean hasForecast() {
        return forecastDates != null && !forecastDates.isEmpty()
                && forecastTemperatures != null && !forecastTemperatures.isEmpty()
                && forecastRains != null && !forecastRains.isEmpty()
                && forecastWinds != null && !forecastWinds.isEmpty()
                && forecastPressures != null && !forecastPressures.isEmpty();  // uwzględnij ciśnienie
    }

    @Override
    public String toString() {
        return "Temperatura: " + temperature + "°C, Opis: " + description;
    }
}
