package com.tomaszwejner.weatherapp;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

public class ChartWindowController {

    @FXML
    private LineChart<String, Number> lineChart;

    private LineChart<String, Number> temperatureChart;

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

    public void addRainSeries(List<String> xValues, List<String> rainValues, LineChart<String, Number> lineChart) {
        XYChart.Series<String, Number> rainSeries = new XYChart.Series<>();
        rainSeries.setName("Opady");

        for (int i = 0; i < xValues.size(); i++) {
            String rainStr = rainValues.get(i);
            double rainDouble;

            if ("brak".equalsIgnoreCase(rainStr)) {
                rainDouble = 0.0;
            } else {
                try {
                    rainDouble = Double.parseDouble(rainStr);
                } catch (NumberFormatException e) {
                    rainDouble = 0.0;
                }
            }

            rainSeries.getData().add(new XYChart.Data<>(xValues.get(i), rainDouble));
        }

        lineChart.getData().add(rainSeries);
    }


    private String resultText;

    public void setResultText(String text) {
        this.resultText = text;
        parseAndShowTempChart();
    }

    private void parseAndShowTempChart() {


        temperatureChart.getData().clear();

        if (resultText == null || resultText.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Temperatura [°C]");

        String[] lines = resultText.split("\\r?\\n");

        int index = 1;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Temperatura")) {
                // Przykład linii: "Temperatura: 20.6"
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        double temp = Double.parseDouble(parts[1].trim());
                        series.getData().add(new XYChart.Data<>("Punkt " + index, temp));
                        index++;
                    } catch (NumberFormatException e) {
                        System.out.println("Niepoprawna wartość temperatury: " + parts[1]);
                    }
                }
            }
        }

        temperatureChart.getData().add(series);
    }
}
