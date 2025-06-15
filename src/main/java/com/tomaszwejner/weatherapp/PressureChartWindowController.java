package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.List;

public class PressureChartWindowController {

    @FXML
    private LineChart<String, Number> pressureChart;

    public void setPressData(List<String> dates, List<Double> press) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ciśnienie [hPa]");

        for (int i = 0; i < dates.size() && i < press.size(); i++) {
            series.getData().add(new XYChart.Data<>(dates.get(i), press.get(i)));
        }

        pressureChart.getData().clear();
        pressureChart.getData().add(series);

        // Automatyczne dopasowanie osi Y do zakresu danych
        double min = press.stream().min(Double::compareTo).orElse(980.0);
        double max = press.stream().max(Double::compareTo).orElse(1040.0);

        ValueAxis<Number> yAxis = (ValueAxis<Number>) pressureChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(min - 5);
        yAxis.setUpperBound(max + 5);
        yAxis.setTickLength(5);

        // Dodaj tooltipy po utworzeniu węzłów
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String x = dates.get(i);
                Double y = press.get(i);

                Tooltip tooltip = new Tooltip(x + ", " + y + " hPa");
                tooltip.setShowDelay(Duration.millis(100));

                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
    }
}
