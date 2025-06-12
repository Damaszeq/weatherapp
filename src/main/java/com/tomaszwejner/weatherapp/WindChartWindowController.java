package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.List;

public class WindChartWindowController {

    @FXML
    private LineChart<String, Number> windChart;

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
}
