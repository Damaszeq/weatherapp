package com.tomaszwejner.weatherapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {

    public String getCurrentWeather(double latitude, double longitude, List<String> parameters) {
        try {
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);

            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&current_weather=true"); // nadal pobieramy "current_weather"
                apiUrl.append("&hourly=").append(joinedParams); // dodajemy parametry do sekcji "hourly"
            } else {
                apiUrl.append("&current_weather=true"); // tylko ogólna pogoda
            }

            URL url = new URL(apiUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsowanie odpowiedzi
            JSONObject json = new JSONObject(response.toString());
            JSONObject currentWeather = json.getJSONObject("current_weather");
            StringBuilder result = new StringBuilder();

            if (parameters.contains("temperature_2m") && currentWeather.has("temperature")) {
                result.append("Temperatura: ").append(currentWeather.getDouble("temperature")).append(" °C\n");
            }
            if (parameters.contains("windspeed_10m") && currentWeather.has("windspeed")) {
                result.append("Prędkość wiatru: ").append(currentWeather.getDouble("windspeed")).append(" km/h\n");
            }

            // Dodatkowe dane z "hourly"
            if (json.has("hourly")) {
                JSONObject hourly = json.getJSONObject("hourly");
                JSONArray timeArray = hourly.getJSONArray("time");
                int latestIndex = timeArray.length() - 1;

                if (parameters.contains("surface_pressure") && hourly.has("surface_pressure")) {
                    JSONArray pressureArray = hourly.getJSONArray("surface_pressure");
                    double pressure = pressureArray.getDouble(latestIndex);
                    result.append("Ciśnienie: ").append(pressure).append(" hPa\n");
                }

                if (parameters.contains("precipitation") && hourly.has("precipitation")) {
                    JSONArray precipitationArray = hourly.getJSONArray("precipitation");
                    double precipitation = precipitationArray.getDouble(latestIndex);
                    result.append("Opady: ").append(precipitation).append(" mm\n");
                }

                if (parameters.contains("soil_temperature_0cm") && hourly.has("soil_temperature_0cm")) {
                    JSONArray soilTempArray = hourly.getJSONArray("soil_temperature_0cm");
                    double soilTemp = soilTempArray.getDouble(latestIndex);
                    result.append("Temperatura gleby: ").append(soilTemp).append(" °C\n");
                }
            }

            // Możesz też wypisać, że inne dane (jak opady, ciśnienie, itp.) będą dostępne z hourly w osobnym etapie

            if (result.isEmpty()) {
                result.append("Brak danych pogodowych dla wybranych parametrów.");
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania pogody.";
        }
    }

}
