package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

public class ChartWindowController {

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    public void initialize() {
        xAxis.setLabel("Data");
        yAxis.setLabel("Wartość");
    }

    public void addSeries(String seriesName, List<String> xValues, List<Double> yValues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (int i = 0; i < xValues.size() && i < yValues.size(); i++) {
            series.getData().add(new XYChart.Data<>(xValues.get(i), yValues.get(i)));
        }

        lineChart.getData().add(series);
    }
}
