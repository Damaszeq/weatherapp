package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


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

    @FXML
    private CheckBox forecastCheckbox;

    @FXML
    private ComboBox<Integer> forecastDaysComboBox;

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

        forecastDaysComboBox.getItems().addAll(1, 3, 5, 7);  // dni prognozy
        forecastDaysComboBox.setValue(3); // domyślnie 3 dni

        // Możesz też dodać listener, żeby ComboBox był aktywny tylko, gdy checkbox jest zaznaczony
        forecastCheckbox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            forecastDaysComboBox.setDisable(!isNowSelected);
        });

        // Wyłącz ComboBox jeśli checkbox nie jest zaznaczony na start
        forecastDaysComboBox.setDisable(!forecastCheckbox.isSelected());
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

    private String getFormattedDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @FXML
    private void onGetWeatherClicked() {
        try {
            List<String> selectedParameters = getSelectedParameters();

            boolean forecast = forecastCheckbox.isSelected();
            int forecastDays = forecastDaysComboBox.getValue();

            if (cityRadioButton.isSelected()) {
                String city = cityTextField.getText();
                if (city == null || city.isEmpty()) {
                    resultLabel.setText("Podaj nazwę miasta.");
                    return;
                }

                Coordinates coords = geoCodingService.getCoordinates(city);
                String weather;

                if (forecast) {
                    LocalDate today = LocalDate.now();
                    LocalDate endDate = today.plusDays(forecastDays);
                    String startDateStr = getFormattedDate(today);
                    String endDateStr = getFormattedDate(endDate);

                    weather = weatherService.getWeatherForecast(coords.latitude, coords.longitude, selectedParameters, startDateStr, endDateStr);
                    resultLabel.setText("Prognoza pogody w " + city + " na " + forecastDays + " dni:\n" + weather);
                } else {
                    weather = weatherService.getCurrentWeather(coords.latitude, coords.longitude, selectedParameters);
                    resultLabel.setText("Pogoda w " + city + ":\n" + weather);
                }


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

                String weather;

                if (forecast) {
                    LocalDate today = LocalDate.now();
                    LocalDate endDate = today.plusDays(forecastDays);
                    String startDateStr = getFormattedDate(today);
                    String endDateStr = getFormattedDate(endDate);

                    weather = weatherService.getWeatherForecast(lat, lon, selectedParameters, startDateStr, endDateStr);
                    resultLabel.setText("Prognoza pogody dla współrzędnych: " + lat + ", " + lon + " na " + forecastDays + " dni:\n" + weather);
                } else {
                    weather = weatherService.getCurrentWeather(lat, lon, selectedParameters);
                    resultLabel.setText("Pogoda dla współrzędnych: " + lat + ", " + lon + "\n" + weather);
                }

            }
        } catch (Exception e) {
            resultLabel.setText("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleShowChart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChartWindow.fxml"));
            Parent root = loader.load();

            ChartWindowController chartController = loader.getController();

            // Przykładowe dane do testu
            List<String> dates = Arrays.asList("2024-06-01", "2024-06-02", "2024-06-03");
            List<Double> temps = Arrays.asList(20.5, 22.3, 19.8);

            chartController.addSeries("Temperatura", dates, temps);

            Stage stage = new Stage();
            stage.setTitle("Wykres danych pogodowych");
            stage.setScene(new Scene(root, 600, 400));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
