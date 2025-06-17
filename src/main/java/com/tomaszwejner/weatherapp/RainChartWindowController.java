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
    private LineChart<String, Number> rainChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label chartTitleLabel;

    private String cityName;
    private String startDate;
    private String endDate;

    @FXML
    public void initialize() {
        xAxis.setLabel("Data");
        yAxis.setLabel("Opady (mm)");
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
        updateTitle();
    }

    public void setMetadata(String cityName, String startDate, String endDate) {
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;
        updateTitle();
    }

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

    private String simplifyDateForTitle(String input) {
        if (input == null) return "";
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1]; // np. "13 czerwca 12:00" → "13 czerwca"
        }
        return input;
    }

    public void addSeries(List<String> xValues, List<Double> yValues) {
        rainChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Opady");

        for (int i = 0; i < xValues.size() && i < yValues.size(); i++) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(xValues.get(i), yValues.get(i));
            series.getData().add(dataPoint);
        }
        rainChart.getData().add(series);

        // Dodaj tooltipy z opadami
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

    @FXML
    private void onExportDataClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik tekstowy", "*.txt"));
        String defaultFileName = String.format("Rain_%s_from_%s_to_%s.txt",
                cityName == null ? "Unknown" : cityName,
                startDate == null ? "start" : startDate.replace(":", "-"),
                endDate == null ? "end" : endDate.replace(":", "-"));
        fileChooser.setInitialFileName(defaultFileName);
        File file = fileChooser.showSaveDialog(rainChart.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.println("Miasto: " + (cityName != null ? cityName : "Nieznane"));
                writer.println();
                for (XYChart.Series<String, Number> series : rainChart.getData()) {
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
