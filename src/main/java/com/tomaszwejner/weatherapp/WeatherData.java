package com.tomaszwejner.weatherapp;

public class WeatherData {
    private final double temperature;
    private final String description;

    public WeatherData(double temperature, String description) {
        this.temperature = temperature;
        this.description = description;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Temperatura: " + temperature + "°C, Opis: " + description;
    }
}
