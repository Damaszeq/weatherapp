package com.tomaszwejner.weatherapp;

public class Coordinates {
    public double latitude;
    public double longitude;

    public Coordinates(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    @Override
    public String toString() {
        return "lat=" + latitude + ", lon=" + longitude;
    }
}
