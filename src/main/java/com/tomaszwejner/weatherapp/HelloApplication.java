package com.tomaszwejner.weatherapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("weather-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Aplikacja pogodowa");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            // Test pobrania współrzędnych Warszawy
            GeoCodingService geoService = new GeoCodingService();
            Coordinates coords = geoService.getCoordinates("Warszawa");
            System.out.println("Współrzędne Warszawy: " + coords);

            // Test pobrania pogody dla współrzędnych
            WeatherService weatherService = new WeatherService();
            String weather = weatherService.getCurrentWeather(coords.latitude, coords.longitude);
            System.out.println(weather);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Uruchomienie GUI
        launch();
    }
}
