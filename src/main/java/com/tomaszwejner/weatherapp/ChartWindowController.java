package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

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
            String x = xValues.get(i);
            Double y = yValues.get(i);

            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(x, y);
            series.getData().add(dataPoint);
        }

        lineChart.getData().add(series);

        // Poczekaj, aż JavaFX stworzy węzły graficzne dla punktów
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> dataPoint = series.getData().get(i);
                String x = xValues.get(i);
                Double y = yValues.get(i);

                Tooltip tooltip = new Tooltip(x + ", " + y + "°C");
                tooltip.setShowDelay(Duration.millis(100));

                // Teraz node już istnieje
                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        });
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
