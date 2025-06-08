package com.tomaszwejner.weatherapp;

import java.util.List;

public class WeatherData {
    private final double temperature;
    private final String description;

    private final List<String> forecastDates;
    private final List<Double> forecastTemperatures;
    private final List<Double> forecastRains;  // nowe pole

    public WeatherData(double temperature, String description,
                       List<String> forecastDates, List<Double> forecastTemperatures,
                       List<Double> forecastRains) {  // rozszerzony konstruktor
        this.temperature = temperature;
        this.description = description;
        this.forecastDates = forecastDates;
        this.forecastTemperatures = forecastTemperatures;
        this.forecastRains = forecastRains;
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

    public List<Double> getForecastRains() {  // getter dla opadów
        return forecastRains;
    }

    public boolean hasForecast() {
        return forecastDates != null && forecastTemperatures != null
                && !forecastDates.isEmpty() && !forecastTemperatures.isEmpty()
                && forecastRains != null && !forecastRains.isEmpty();  // uwzględnij opady
    }

    @Override
    public String toString() {
        return "Temperatura: " + temperature + "°C, Opis: " + description;
    }
}
