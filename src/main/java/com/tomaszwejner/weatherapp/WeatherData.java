package com.tomaszwejner.weatherapp;

import java.util.List;

public class WeatherData {
    private final double temperature;
    private final String description;

    private final List<String> forecastDates;
    private final List<Double> forecastTemperatures;
    private final List<Double> forecastRains;
    private final List<Double> forecastWinds;  // nowe pole: prędkość wiatru

    public WeatherData(double temperature, String description,
                       List<String> forecastDates, List<Double> forecastTemperatures,
                       List<Double> forecastRains, List<Double> forecastWinds) {  // rozszerzony konstruktor
        this.temperature = temperature;
        this.description = description;
        this.forecastDates = forecastDates;
        this.forecastTemperatures = forecastTemperatures;
        this.forecastRains = forecastRains;
        this.forecastWinds = forecastWinds;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
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

    public List<Double> getForecastWinds() {  // getter dla wiatru
        return forecastWinds;
    }

    public boolean hasForecast() {
        return forecastDates != null && !forecastDates.isEmpty()
                && forecastTemperatures != null && !forecastTemperatures.isEmpty()
                && forecastRains != null && !forecastRains.isEmpty()
                && forecastWinds != null && !forecastWinds.isEmpty();  // uwzględnij wiatr
    }

    @Override
    public String toString() {
        return "Temperatura: " + temperature + "°C, Opis: " + description;
    }
}
