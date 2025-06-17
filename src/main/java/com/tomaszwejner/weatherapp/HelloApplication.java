package com.tomaszwejner.weatherapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ładuje plik FXML i tworzy scenę o wymiarach 600x700 pikseli
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("weather-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 700);

        // Ustawia tytuł okna
        stage.setTitle("Aplikacja pogodowa");

        // Ustawia scenę w oknie
        stage.setScene(scene);

        // Wyświetla okno
        stage.show();
    }

    public static void main(String[] args) {
        // Uruchamia aplikację JavaFX (wywołuje start())
        launch();
    }
}
