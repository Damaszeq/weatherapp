package com.tomaszwejner.weatherapp;

// Prosta klasa przechowująca współrzędne geograficzne: szerokość i długość geograficzną
public class Coordinates {
    public double latitude;
    public double longitude;

    // Konstruktor inicjalizujący współrzędne
    public Coordinates(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    // Metoda do czytelnego wypisania współrzędnych w formacie "lat=..., lon=..."
    @Override
    public String toString() {
        return "lat=" + latitude + ", lon=" + longitude;
    }
}
