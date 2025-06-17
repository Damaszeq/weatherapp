package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Kontroler okna wykresu w aplikacji pogodowej.
 * Odpowiada za inicjalizację wykresu, dodawanie serii danych,
 * wyświetlanie tooltipów oraz eksport danych do pliku.
 */
public class ChartWindowController {

    // Komponenty powiązane z FXML
    @FXML
    private LineChart<String, Number> lineChart;  // główny wykres liniowy

    @FXML
    private CategoryAxis xAxis; // oś X - kategorie (daty)

    @FXML
    private NumberAxis yAxis;   // oś Y - wartości liczbowe

    @FXML
    private Label chartTitleLabel; // Label z tytułem wykresu

    // Dane metadanych wykresu
    private String cityName;
    private String startDate;
    private String endDate;

    // Tekst z wynikami do parsowania (np. temperatury)
    private String resultText;

    // Inicjalizacja kontrolera, ustawienie etykiet osi
    @FXML
    public void initialize() {
        xAxis.setLabel("Data");
        yAxis.setLabel("Wartość");
    }

    /**
     * Ustawia nazwę miasta i aktualizuje tytuł wykresu.
     * @param cityName nazwa miasta
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
        if (chartTitleLabel != null) {
            chartTitleLabel.setText("Pogoda dla: " + cityName);
        }
    }

    public String getCityName() {
        return cityName;
    }

    /**
     * Ustawia metadane wykresu i aktualizuje tytuł z datami.
     * @param cityName nazwa miasta
     * @param startDate data początkowa (np. "13 czerwca 12:00")
     * @param endDate data końcowa
     */
    public void setMetadata(String cityName, String startDate, String endDate) {
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;

        if (chartTitleLabel != null) {
            String simplifiedStart = simplifyDateForTitle(startDate);
            String simplifiedEnd = simplifyDateForTitle(endDate);
            chartTitleLabel.setText(String.format("Temperatura dla %s od %s do %s", cityName, simplifiedStart, simplifiedEnd));
        }
    }

    /**
     * Pomocnicza metoda do uproszczenia daty w tytule (np. usuwa godzinę).
     * @param input oryginalna data
     * @return uproszczona data
     */
    private String simplifyDateForTitle(String input) {
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1]; // np. "13 czerwca"
        }
        return input;
    }

    /**
     * Dodaje serię danych do wykresu.
     * Tworzy tooltipy dla każdego punktu wykresu z wartościami.
     * @param seriesName nazwa serii (np. "Temperatura")
     * @param xValues lista wartości osi X (daty)
     * @param yValues lista wartości osi Y (np. temperatura)
     */
    public void addSeries(String seriesName, List<String> xValues, List<Double> yValues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (int i = 0; i < xValues.size() && i < yValues.size(); i++) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(xValues.get(i), yValues.get(i));
            series.getData().add(dataPoint);
        }

        lineChart.getData().add(series);

        // Dodanie tooltipów po renderowaniu węzłów JavaFX
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String x = xValues.get(i);
                Double y = yValues.get(i);

                Tooltip tooltip = new Tooltip(x + ", " + y + "°C");
                tooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
    }

    /**
     * Ustawia surowy tekst z wynikami (np. z API) i uruchamia parsowanie do wykresu temperatur.
     * @param text surowy tekst z danymi
     */
    public void setResultText(String text) {
        this.resultText = text;
        parseAndShowTempChart();
    }

    /**
     * Parsuje resultText, wyciąga wartości temperatur i dodaje je do wykresu.
     * Tu można rozbudować obsługę innych typów danych.
     */
    private void parseAndShowTempChart() {
        // Czyszczenie istniejących danych
        lineChart.getData().clear();

        if (resultText == null || resultText.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Temperatura [°C]");

        String[] lines = resultText.split("\\r?\\n");
        int index = 1;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Temperatura")) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        double temp = Double.parseDouble(parts[1].trim());
                        series.getData().add(new XYChart.Data<>("Punkt " + index, temp));
                        index++;
                    } catch (NumberFormatException e) {
                        System.err.println("Niepoprawna wartość temperatury: " + parts[1]);
                    }
                }
            }
        }

        lineChart.getData().add(series);
    }

    /**
     * Handler przycisku eksportu danych do pliku tekstowego.
     * Pozwala użytkownikowi wybrać lokalizację i zapisuje dane wykresu.
     */
    @FXML
    private void onExportDataClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik tekstowy", "*.txt"));

        // Nazwa pliku bazująca na danych wykresu
        String defaultFileName = String.format("Temperature_%s_from_%s_to_%s.txt",
                cityName != null ? cityName.replaceAll("\\s+", "_") : "unknown",
                startDate != null ? startDate.replaceAll("\\s+", "_") : "start",
                endDate != null ? endDate.replaceAll("\\s+", "_") : "end");
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(lineChart.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.println("Miasto: " + (cityName != null ? cityName : "Nieznane"));
                writer.println();
                for (XYChart.Series<String, Number> series : lineChart.getData()) {
                    writer.println("Seria: " + series.getName());
                    writer.println("Czas;Wartość");
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        writer.println(data.getXValue() + ";" + data.getYValue());
                    }
                    writer.println();
                }

                // Informacja o powodzeniu zapisu
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dane zostały wyeksportowane.");
                alert.showAndWait();
            } catch (IOException e) {
                // Obsługa błędu zapisu - pokazanie alertu z komunikatem
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas zapisu danych: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
