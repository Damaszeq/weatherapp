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

public class WindChartWindowController {

    public CategoryAxis xAxis;
    public NumberAxis yAxis;
    private String cityName;
    private String startDate;
    private String endDate;

    @FXML
    Label chartTitleLabel;
    @FXML
    private LineChart<String, Number> windChart;

    private String simplifyDateForTitle(String input) {
        if (input == null) return "";
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1]; // np. "13 czerwca 12:00" → "13 czerwca"
        }
        return input;
    }

    private void updateTitle() {
        if (chartTitleLabel != null) {
            String simplifiedStart = simplifyDateForTitle(startDate);
            String simplifiedEnd = simplifyDateForTitle(endDate);
            if (cityName != null && startDate != null && endDate != null) {
                chartTitleLabel.setText(String.format("Predkosc wiatru w %s od %s do %s", cityName, simplifiedStart, simplifiedEnd));
            } else if (cityName != null) {
                chartTitleLabel.setText("Predkosc wiatru w " + cityName);
            } else {
                chartTitleLabel.setText("Wykres predkosci wiatru");
            }
        }
    }
    public void setMetadata(String cityName, String startDate, String endDate) {
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;
        updateTitle();
    }

    public void setWindData(List<String> dates, List<Double> windSpeeds) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prędkość wiatru [km/h]");

        for (int i = 0; i < dates.size() && i < windSpeeds.size(); i++) {
            series.getData().add(new XYChart.Data<>(dates.get(i), windSpeeds.get(i)));
        }

        windChart.getData().clear();
        windChart.getData().add(series);

        // Dodaj tooltipy po utworzeniu węzłów
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String x = dates.get(i);
                Double y = windSpeeds.get(i);

                Tooltip tooltip = new Tooltip(x + ", " + y + " km/h");
                tooltip.setShowDelay(Duration.millis(100));

                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
    }
    @FXML
    private void onExportDataClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik tekstowy", "*.txt"));
        String defaultFileName = String.format("Wind_%s_from_%s_to_%s.txt",
                cityName == null ? "Unknown" : cityName,
                startDate == null ? "start" : startDate.replace(":", "-"),
                endDate == null ? "end" : endDate.replace(":", "-"));
        fileChooser.setInitialFileName(defaultFileName);
        File file = fileChooser.showSaveDialog(windChart.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                for (XYChart.Series<String, Number> series : windChart.getData()) {
                    writer.println("Seria: " + series.getName());
                    writer.println("Data;Opady (mm)");
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        writer.println(data.getXValue() + ";" + data.getYValue());
                    }
                    writer.println();
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dane zostały wyeksportowane.");
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas zapisu danych: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}



