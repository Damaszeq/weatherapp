package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

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

    }
}
