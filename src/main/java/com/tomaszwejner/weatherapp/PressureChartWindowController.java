package com.tomaszwejner.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PressureChartWindowController {

    // Zmienne przechowujące metadane do wykresu
    private String cityName;
    private String startDate;
    private String endDate;

    @FXML
    Label chartTitleLabel; // Etykieta tytułu wykresu

    public CategoryAxis xAxis; // Oś kategorii (daty)
    public NumberAxis yAxis;   // Oś liczbowa (ciśnienie)

    @FXML
    private LineChart<String, Number> pressureChart; // Wykres liniowy ciśnienia

    // Ustawia metadane i aktualizuje tytuł wykresu
    public void setMetadata(String cityName, String startDate, String endDate) {
        this.cityName = cityName;
        this.startDate = startDate;
        this.endDate = endDate;
        updateTitle();
    }

    // Aktualizuje tytuł wykresu na podstawie metadanych
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

    // Upraszcza datę do formatu "dzień miesiąc" (usuwając np. godzinę)
    private String simplifyDateForTitle(String input) {
        if (input == null) return "";
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return input;
    }

    // Ustawia dane ciśnienia i tworzy serię na wykresie
    public void setPressData(List<String> dates, List<Double> press) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ciśnienie [hPa]");

        // Dodaje punkty danych do serii
        for (int i = 0; i < dates.size() && i < press.size(); i++) {
            series.getData().add(new XYChart.Data<>(dates.get(i), press.get(i)));
        }

        // Czyści poprzednie dane i dodaje nową serię do wykresu
        pressureChart.getData().clear();
        pressureChart.getData().add(series);

        // Automatycznie ustawia zakres osi Y na podstawie minimalnego i maksymalnego ciśnienia
        double min = press.stream().min(Double::compareTo).orElse(980.0);
        double max = press.stream().max(Double::compareTo).orElse(1040.0);

        ValueAxis<Number> yAxis = (ValueAxis<Number>) pressureChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(min - 5); // zakres trochę rozszerzony na dole
        yAxis.setUpperBound(max + 5); // zakres rozszerzony na górze
        yAxis.setTickLength(5);       // długość podziałek osi

        // Dodaje tooltipy z informacją o wartości ciśnienia przy punktach na wykresie
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

    // Obsługuje kliknięcie przycisku eksportu danych do pliku
    @FXML
    private void onExportDataClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik tekstowy", "*.txt"));

        // Domyślna nazwa pliku oparta o metadane (miasto, daty)
        String defaultFileName = String.format("Rain_%s_from_%s_to_%s.txt",
                cityName == null ? "Unknown" : cityName,
                startDate == null ? "start" : startDate.replace(":", "-"),
                endDate == null ? "end" : endDate.replace(":", "-"));
        fileChooser.setInitialFileName(defaultFileName);

        // Otwiera okno dialogowe zapisu pliku
        File file = fileChooser.showSaveDialog(pressureChart.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // Zapisuje metadane
                writer.println("Miasto: " + (cityName != null ? cityName : "Nieznane"));
                writer.println();

                // Zapisuje dane wykresu (serie i punkty)
                for (XYChart.Series<String, Number> series : pressureChart.getData()) {
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
