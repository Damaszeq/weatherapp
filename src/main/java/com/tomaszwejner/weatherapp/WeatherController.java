package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;

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
    private Label resultLabel;
    
    @FXML
    private CheckBox temperatureCheckbox;

    @FXML
    private CheckBox soilTempCheckbox;

    @FXML
    private CheckBox windCheckbox;

    @FXML
    private CheckBox rainCheckbox;

    @FXML
    private CheckBox pressureCheckbox;

    private final GeoCodingService geoCodingService = new GeoCodingService();
    private final WeatherService weatherService = new WeatherService();

    @FXML
    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        cityRadioButton.setToggleGroup(toggleGroup);
        coordsRadioButton.setToggleGroup(toggleGroup);

        cityRadioButton.setSelected(true);
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            toggleInputFields();
        });

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
        resultLabel.setText("");
    }

    @FXML
    private List<String> getSelectedParameters() {
        List<String> parameters = new ArrayList<>();

        if (temperatureCheckbox.isSelected()) parameters.add("temperature_2m");
        if (soilTempCheckbox.isSelected()) parameters.add("soil_temperature_0cm");
        if (windCheckbox.isSelected()) parameters.add("windspeed_10m");
        if (rainCheckbox.isSelected()) parameters.add("precipitation");
        if (pressureCheckbox.isSelected()) parameters.add("surface_pressure");

        return parameters;
    }

    @FXML
    private void onGetWeatherClicked() {
        try {
            List<String> selectedParameters = getSelectedParameters();

            if (cityRadioButton.isSelected()) {
                String city = cityTextField.getText();
                if (city == null || city.isEmpty()) {
                    resultLabel.setText("Podaj nazwę miasta.");
                    return;
                }

                Coordinates coords = geoCodingService.getCoordinates(city);
                String weather = weatherService.getCurrentWeather(coords.latitude, coords.longitude, selectedParameters);
                resultLabel.setText("Pogoda w " + city + ":\n" + weather);

            } else if (coordsRadioButton.isSelected()) {
                String latText = latitudeTextField.getText();
                String lonText = longitudeTextField.getText();

                if (latText == null || latText.isEmpty() || lonText == null || lonText.isEmpty()) {
                    resultLabel.setText("Podaj szerokość i długość geograficzną.");
                    return;
                }

                double lat, lon;
                try {
                    lat = Double.parseDouble(latText);
                    lon = Double.parseDouble(lonText);
                } catch (NumberFormatException e) {
                    resultLabel.setText("Szerokość i długość muszą być liczbami.");
                    return;
                }

                String weather = weatherService.getCurrentWeather(lat, lon, selectedParameters);
                resultLabel.setText("Pogoda dla współrzędnych: " + lat + ", " + lon + "\n" + weather);
            }
        } catch (Exception e) {
            resultLabel.setText("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
