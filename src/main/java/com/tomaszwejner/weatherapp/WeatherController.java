package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class WeatherController {

    @FXML
    private RadioButton cityRadioButton;

    @FXML
    private RadioButton coordsRadioButton;

    @FXML
    private TextField cityTextField;

    @FXML
    private TextField latitudeTextField;

    @FXML
    private TextField longitudeTextField;

    @FXML
    private Button getWeatherButton;

    @FXML
    private Label weatherLabel;

    private final GeoCodingService geoCodingService = new GeoCodingService();
    private final WeatherService weatherService = new WeatherService();

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        // Grupowanie radio buttonów, by tylko jeden mógł być wybrany
        toggleGroup = new ToggleGroup();
        cityRadioButton.setToggleGroup(toggleGroup);
        coordsRadioButton.setToggleGroup(toggleGroup);

        // Domyślnie zaznacz wyszukiwanie po mieście
        cityRadioButton.setSelected(true);

        // Dodaj listener na zmianę wyboru
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            toggleInputFields();
        });

        // Ustaw widoczność pól na start
        toggleInputFields();
    }

    private void toggleInputFields() {
        boolean citySelected = cityRadioButton.isSelected();

        // Pole miasta widoczne tylko gdy wyszukujemy po mieście
        cityTextField.setDisable(!citySelected);
        cityTextField.setVisible(citySelected);

        // Pola szerokości i długości widoczne tylko gdy wyszukujemy po współrzędnych
        latitudeTextField.setDisable(citySelected);
        latitudeTextField.setVisible(!citySelected);

        longitudeTextField.setDisable(citySelected);
        longitudeTextField.setVisible(!citySelected);

        // Czyść labelkę przy zmianie metody wyszukiwania
        weatherLabel.setText("");
    }

    @FXML
    private void onGetWeatherClicked() {
        try {
            if (cityRadioButton.isSelected()) {
                String city = cityTextField.getText();
                if (city == null || city.isEmpty()) {
                    weatherLabel.setText("Podaj nazwę miasta.");
                    return;
                }

                Coordinates coords = geoCodingService.getCoordinates(city);
                String weather = weatherService.getCurrentWeather(coords.latitude, coords.longitude);
                weatherLabel.setText("Pogoda w " + city + ":\n" + weather);

            } else if (coordsRadioButton.isSelected()) {
                String latText = latitudeTextField.getText();
                String lonText = longitudeTextField.getText();

                if (latText == null || latText.isEmpty() || lonText == null || lonText.isEmpty()) {
                    weatherLabel.setText("Podaj szerokość i długość geograficzną.");
                    return;
                }

                double lat, lon;
                try {
                    lat = Double.parseDouble(latText);
                    lon = Double.parseDouble(lonText);
                } catch (NumberFormatException e) {
                    weatherLabel.setText("Szerokość i długość muszą być liczbami.");
                    return;
                }

                String weather = weatherService.getCurrentWeather(lat, lon);
                weatherLabel.setText("Pogoda dla współrzędnych: " + lat + " " + lon + "\n" + weather);

            }
        } catch (Exception e) {
            weatherLabel.setText("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
