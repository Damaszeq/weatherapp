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

public class RainChartWindowController {

    @FXML
    private LineChart<String, Number> rainChart; // Wykres liniowy opadów

    @FXML
    private CategoryAxis xAxis; // Oś kategorii (daty)

    @FXML
    private NumberAxis yAxis;   // Oś liczbowa (wartości opadów)

    @FXML
    private Label chartTitleLabel; // Etykieta tytułu wykresu

    private String cityName;
    private String startDate;
    private String endDate;

    // Inicjalizacja kontrolki po załadowaniu FXML
    @FXML
    public void initialize() {
        xAxis.setLabel("Data");        // Etykieta osi X
        yAxis.setLabel("Opady (mm)");  // Etykieta osi Y
    }

    // Ustawia nazwę miasta i aktualizuje tytuł wykresu
    public void setCityName(String cityName) {
        this.cityName = cityName;
        updateTitle();
    }

    // Ustawia metadane i aktualizuje tytuł wykresu
    public void setMetadata(String cityName, String startDate, String endDate) {
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;
        updateTitle();
    }

    // Aktualizuje tytuł wykresu na podstawie dostępnych metadanych
    private void updateTitle() {
        if (chartTitleLabel != null) {
            String simplifiedStart = simplifyDateForTitle(startDate);
            String simplifiedEnd = simplifyDateForTitle(endDate);
            if (cityName != null && startDate != null && endDate != null) {
                chartTitleLabel.setText(String.format("Opady w %s od %s do %s", cityName, simplifiedStart, simplifiedEnd));
            } else if (cityName != null) {
                chartTitleLabel.setText("Opady w " + cityName);
            } else {
                chartTitleLabel.setText("Wykres opadów");
            }
        }
    }

    // Upraszcza datę do formatu "dzień miesiąc" (np. usuwając godzinę)
    private String simplifyDateForTitle(String input) {
        if (input == null) return "";
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return input;
    }

    // Dodaje serię danych do wykresu (daty i wartości opadów)
    public void addSeries(List<String> xValues, List<Double> yValues) {
        rainChart.getData().clear(); // Czyści poprzednie serie

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Opady");

        // Dodaje punkty danych do serii
        for (int i = 0; i < xValues.size() && i < yValues.size(); i++) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(xValues.get(i), yValues.get(i));
            series.getData().add(dataPoint);
        }

        rainChart.getData().add(series); // Dodaje serię do wykresu

        // Dodaje tooltipy przy punktach wykresu z informacją o opadach
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String date = xValues.get(i);
                Double rainValue = yValues.get(i);

                Tooltip tooltip = new Tooltip(date + ", " + rainValue + " mm");
                tooltip.setShowDelay(Duration.millis(100));

                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
    }

    // Obsługuje kliknięcie przycisku eksportu danych do pliku
    @FXML
    private void onExportDataClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik tekstowy", "*.txt"));

        // Domyślna nazwa pliku oparta na metadanych (miasto, daty)
        String defaultFileName = String.format("Rain_%s_from_%s_to_%s.txt",
                cityName == null ? "Unknown" : cityName,
                startDate == null ? "start" : startDate.replace(":", "-"),
                endDate == null ? "end" : endDate.replace(":", "-"));
        fileChooser.setInitialFileName(defaultFileName);

        // Otwiera okno dialogowe zapisu pliku
        File file = fileChooser.showSaveDialog(rainChart.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // Zapisuje metadane
                writer.println("Miasto: " + (cityName != null ? cityName : "Nieznane"));
                writer.println();

                // Zapisuje dane wykresu (serie i punkty)
                for (XYChart.Series<String, Number> series : rainChart.getData()) {
                    writer.println("Seria: " + series.getName());
                    writer.println("Data;Opady (mm)");
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        writer.println(data.getXValue() + ";" + data.getYValue());
                    }
                    writer.println();
                }

                // Informuje użytkownika o sukcesie
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dane zostały wyeksportowane.");
                alert.showAndWait();
            } catch (IOException e) {
                // Informuje użytkownika o błędzie zapisu
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas zapisu danych: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
