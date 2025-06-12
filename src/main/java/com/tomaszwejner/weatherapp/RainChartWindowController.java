package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.List;

public class RainChartWindowController {

    @FXML
    private LineChart<String, Number> rainChart;

    public void setRainData(List<String> dates, List<Double> rains) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Opady [mm]");

        for (int i = 0; i < dates.size() && i < rains.size(); i++) {
            series.getData().add(new XYChart.Data<>(dates.get(i), rains.get(i)));
        }

        rainChart.getData().clear();
        rainChart.getData().add(series);

        // Dodaj tooltipy po utworzeniu węzłów
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String x = dates.get(i);
                Double y = rains.get(i);

                Tooltip tooltip = new Tooltip(x + ", " + y + " mm");
                tooltip.setShowDelay(Duration.millis(100));

                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
    }

}
