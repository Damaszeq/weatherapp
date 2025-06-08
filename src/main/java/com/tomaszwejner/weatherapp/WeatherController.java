package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @FXML
    private Button selectAllButton;

    @FXML
    private Button showChartButton;

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
        selectAllButton.setText("Zaznacz wszystko");
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            toggleInputFields();
        });

        toggleInputFields();

        forecastDaysComboBox.getItems().clear();
        for (int i = 1; i <= 16; i++) {
            forecastDaysComboBox.getItems().add(i);
        }
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
    private void onSelectAllClicked() {
        boolean allSelected = temperatureCheckbox.isSelected()
                && soilTempCheckbox.isSelected()
                && windCheckbox.isSelected()
                && rainCheckbox.isSelected()
                && pressureCheckbox.isSelected();

        boolean newState = !allSelected; // jeśli wszystkie zaznaczone to odznaczamy, jeśli nie - zaznaczamy

        temperatureCheckbox.setSelected(newState);
        soilTempCheckbox.setSelected(newState);
        windCheckbox.setSelected(newState);
        rainCheckbox.setSelected(newState);
        pressureCheckbox.setSelected(newState);

        // Opcjonalnie zmień tekst przycisku
        if (newState) {
            selectAllButton.setText("Odznacz wszystko");
        } else {
            selectAllButton.setText("Zaznacz wszystko");
        }
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

                // **Tu wywołujemy reverse geocoding, żeby poznać nazwę najbliższego miasta**
                String cityName = "Nieznana lokalizacja";
                try {
                    cityName = geoCodingService.getCityName(lat, lon);
                } catch (Exception e) {
                    // Jeśli coś pójdzie nie tak, wyświetlamy "Nieznana lokalizacja"
                    System.err.println("Błąd reverse geocoding: " + e.getMessage());
                }

                String weather;

                if (forecast) {
                    LocalDate today = LocalDate.now();
                    LocalDate endDate = today.plusDays(forecastDays);
                    String startDateStr = getFormattedDate(today);
                    String endDateStr = getFormattedDate(endDate);

                    weather = weatherService.getWeatherForecast(lat, lon, selectedParameters, startDateStr, endDateStr);
                    resultLabel.setText("Prognoza pogody dla współrzędnych: " + lat + ", " + lon +
                            " (" + cityName + ") na " + forecastDays + " dni:\n" + weather);
                } else {
                    weather = weatherService.getCurrentWeather(lat, lon, selectedParameters);
                    resultLabel.setText("Pogoda dla współrzędnych: " + lat + ", " + lon +
                            " (" + cityName + ")\n" + weather);
                }
            }
        } catch (Exception e) {
            resultLabel.setText("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private WeatherData parseWeatherDataFromResultLabel() {
        String text = resultLabel.getText();
        List<String> dates = new ArrayList<>();
        List<Double> temps = new ArrayList<>();
        List<Double> rains = new ArrayList<>();

        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Znajdź linie z datą
            if (line.matches("\\d{1,2} \\p{L}+ \\d{2}:\\d{2}:?")) {
                dates.add(line.replace(":", ""));

                // Znajdź indeks kolejnej daty lub koniec tablicy
                int nextDateIndex = lines.length;
                for (int k = i + 1; k < lines.length; k++) {
                    if (lines[k].trim().matches("\\d{1,2} \\p{L}+ \\d{2}:\\d{2}:?")) {
                        nextDateIndex = k;
                        break;
                    }
                }

                // Przeszukaj linie między i+1 a nextDateIndex - 1
                double temp = 0.0;
                double rain = 0.0;
                for (int j = i + 1; j < nextDateIndex; j++) {
                    String currentLine = lines[j].trim();

                    // Temperatura
                    if (currentLine.toLowerCase().startsWith("temperatura")) {
                        String tempStr = currentLine.replaceAll("[^0-9.,-]", "").replace(',', '.');
                        try {
                            temp = Double.parseDouble(tempStr);
                        } catch (NumberFormatException e) {
                            System.out.println("Nie udało się sparsować temperatury: " + tempStr);
                        }
                    }

                    // Opady
                    if (currentLine.toLowerCase().startsWith("opad")) {
                        String rainStr = currentLine.replaceAll("[^0-9.,-]", "").replace(',', '.');
                        if (rainStr.isEmpty()) {
                            rain = 0.0;
                        } else {
                            try {
                                rain = Double.parseDouble(rainStr);
                            } catch (NumberFormatException e) {
                                System.out.println("Nie udało się sparsować opadów: " + rainStr);
                            }
                        }
                    }
                }
                temps.add(temp);
                rains.add(rain);
            }
        }

        double currentTemp = temps.isEmpty() ? 0.0 : temps.get(0);
        String description = "Pogoda z resultLabel";

        return new WeatherData(currentTemp, description, dates, temps, rains);
    }



    @FXML
    public void handleShowChart() {
        WeatherData data = parseWeatherDataFromResultLabel();
        showChartWithData(data);
    }

    private void showChartWithData(WeatherData data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tomaszwejner/weatherapp/ChartWindow.fxml"));
            Parent root = loader.load();

            ChartWindowController chartController = loader.getController();

            if (data == null || !data.hasForecast()) {
                System.out.println("Brak danych do wyświetlenia wykresu.");
            } else {
                chartController.addSeries("Prognoza temperatury", data.getForecastDates(), data.getForecastTemperatures());
                //chartController.addSeries("Opady", data.getForecastDates(), data.getForecastRains());
            }

            Stage stage = new Stage();
            stage.setTitle("Wykres pogody");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowRainChart() {
        WeatherData data = parseWeatherDataFromResultLabel();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tomaszwejner/weatherapp/RainChart.fxml"));
            Parent root = loader.load();

            RainChartWindowController rainController = loader.getController();

            rainController.setRainData(data.getForecastDates(), data.getForecastRains());

            Stage stage = new Stage();
            stage.setTitle("Wykres opadów");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


