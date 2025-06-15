package com.tomaszwejner.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class WeatherService {

    private String getLabelForParameter(String param) {
        return switch (param) {
            case "temperature_2m" -> "Temperatura";
            case "relative_humidity_2m" -> "Wilgotność";
            case "soil_temperature_0cm" -> "Temperatura gleby";
            case "windspeed_10m" -> "Wiatr";
            case "precipitation" -> "Opady";
            case "surface_pressure" -> "Ciśnienie";
            default -> param;
        };
    }

    private String getUnitForParameter(String param) {
        return switch (param) {
            case "temperature_2m" -> " °C";
            case "relative_humidity_2m" -> "%";
            case "wind_speed_10m" -> " km/h";
            case "precipitation" -> " mm";
            case "pressure_msl" -> " hPa";
            default -> "";
        };
    }

    public String getCurrentWeather(double latitude, double longitude, List<String> parameters) {
        try {
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);

            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&current_weather=true");
                apiUrl.append("&hourly=").append(joinedParams);
            } else {
                apiUrl.append("&current_weather=true");
            }

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

            JSONObject json = new JSONObject(response.toString());
            JSONObject currentWeather = json.getJSONObject("current_weather");
            StringBuilder result = new StringBuilder();

            if (parameters.contains("temperature_2m") && currentWeather.has("temperature")) {
                result.append("Temperatura: ").append(currentWeather.getDouble("temperature")).append(" °C\n");
            }
            if (parameters.contains("windspeed_10m") && currentWeather.has("windspeed")) {
                result.append("Prędkość wiatru: ").append(currentWeather.getDouble("windspeed")).append(" km/h\n");
            }

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

            JSONObject json = new JSONObject(response.toString());

            if (!json.has("hourly")) {
                return "Brak danych prognozy pogodowej dla wybranych parametrów.";
            }

            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < timeArray.length(); i++) {
                String time = timeArray.getString(i);

                LocalDateTime dateTime = LocalDateTime.parse(time);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("pl"));
                String formattedTime = dateTime.format(formatter);

                result.append(formattedTime).append(":\n");

                for (String param : parameters) {
                    if (hourly.has(param)) {
                        JSONArray paramArray = hourly.getJSONArray(param);
                        Object val = paramArray.get(i);

                        String formattedValue;
                        if (val == JSONObject.NULL) {
                            formattedValue = "brak danych";
                        } else if (val instanceof Number) {
                            double number = ((Number) val).doubleValue();

                            if (param.toLowerCase().contains("precipitation") && number == 0.0) {
                                formattedValue = "brak";
                            } else {
                                formattedValue = number + getUnitForParameter(param);
                            }
                        } else {
                            formattedValue = val.toString();
                        }

                        result.append("  ").append(getLabelForParameter(param)).append(": ").append(formattedValue).append("\n");
                    }
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania prognozy pogody. Jeśli pobierasz prognozę pogody, spróbuj zmniejszając liczbę dni";
        }
    }
    public String getHistoricalWeather(double latitude, double longitude, List<String> parameters, int pastDays) {
        try {
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);
            apiUrl.append("&past_days=").append(pastDays);
            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&hourly=").append(joinedParams);
            }

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

            JSONObject json = new JSONObject(response.toString());

            if (!json.has("hourly")) {
                return "Brak danych historycznych dla wybranych parametrów.";
            }

            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < timeArray.length(); i++) {
                String time = timeArray.getString(i);
                LocalDateTime dateTime = LocalDateTime.parse(time);

                // odrzucamy dane, które są po wczoraj
                if (dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(1))) {
                    continue;  // pomijamy te dane (czyli przyszłe)
                }

                // dalej Twój istniejący kod formatowania i wypisywania danych
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("pl"));
                String formattedTime = dateTime.format(formatter);

                result.append(formattedTime).append(":\n");

                for (String param : parameters) {
                    if (hourly.has(param)) {
                        JSONArray paramArray = hourly.getJSONArray(param);
                        Object val = paramArray.get(i);

                        String formattedValue;
                        if (val == JSONObject.NULL) {
                            formattedValue = "brak danych";
                        } else if (val instanceof Number) {
                            double number = ((Number) val).doubleValue();

                            if (param.toLowerCase().contains("precipitation") && number == 0.0) {
                                formattedValue = "brak";
                            } else {
                                formattedValue = number + getUnitForParameter(param);
                            }
                        } else {
                            formattedValue = val.toString();
                        }

                        result.append("  ").append(getLabelForParameter(param)).append(": ").append(formattedValue).append("\n");
                    }
                }

                result.append("\n");
            }


            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania danych historycznych.";
        }
    }


}
