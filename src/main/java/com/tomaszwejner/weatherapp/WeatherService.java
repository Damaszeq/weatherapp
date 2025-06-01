package com.tomaszwejner.weatherapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public String getWeatherForecast(double latitude, double longitude, List<String> parameters, String startDate, String endDate) {
        try {
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);
            apiUrl.append("&start_date=").append(startDate);
            apiUrl.append("&end_date=").append(endDate);

            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&hourly=").append(joinedParams);
            }

            // Nie dodawaj tutaj current_weather=true, bo to jest prognoza na dni, nie aktualna pogoda

            URL url = new URL(apiUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsowanie odpowiedzi z pola "hourly"
            JSONObject json = new JSONObject(response.toString());

            if (!json.has("hourly")) {
                return "Brak danych prognozy pogodowej dla wybranych parametrów.";
            }

            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < timeArray.length(); i++) {
                String time = timeArray.getString(i);
                result.append(time).append(": ");

                for (String param : parameters) {
                    if (hourly.has(param)) {
                        JSONArray paramArray = hourly.getJSONArray(param);
                        Object val = paramArray.get(i);
                        if (val == JSONObject.NULL) {
                            result.append(param).append("=null ");
                        } else if (val instanceof Number) {
                            result.append(param).append("=").append(paramArray.getDouble(i)).append(" ");
                        } else {
                            result.append(param).append("=").append(val.toString()).append(" ");
                        }
                    }
                }
                result.append("\n");
            }


            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania prognozy pogody.";
        }
    }



}
