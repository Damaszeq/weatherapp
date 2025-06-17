package com.tomaszwejner.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

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

    private final Jedis jedis = new Jedis("localhost", 6379);
    private final int DEFAULT_TTL_SECONDS = 3600; // 1 godzina
    // Metoda pomocnicza: zwraca polską etykietę dla danego parametru pogodowego
    private String getLabelForParameter(String param) {
        return switch (param) {
            case "temperature_2m" -> "Temperatura";
            case "relative_humidity_2m" -> "Wilgotność";
            case "soil_temperature_0cm" -> "Temperatura gleby";
            case "windspeed_10m" -> "Wiatr";
            case "precipitation" -> "Opady";
            case "surface_pressure" -> "Ciśnienie";
            default -> param; // Jeśli brak dopasowania, zwraca oryginalną nazwę
        };
    }

    private String generateCacheKey(String prefix, double latitude, double longitude, List<String> parameters, String... extra) {
        String key = prefix + ":" + latitude + ":" + longitude + ":" + String.join(",", parameters);
        for (String s : extra) {
            key += ":" + s;
        }
        return key;
    }

    // Metoda pomocnicza: zwraca jednostkę miary dla danego parametru pogodowego
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

    // METODA 1: Pobiera aktualne dane pogodowe z API Open-Meteo
    public String getCurrentWeather(double latitude, double longitude, List<String> parameters) {
        try {
            String cacheKey = generateCacheKey("current", latitude, longitude, parameters);
            String cached = jedis.get(cacheKey);
            if (cached != null) {
                return cached + " (z cache)\n";
            }
            // Budujemy URL zapytania
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);

            // Jeśli są parametry - dodajemy je do zapytania
            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&current_weather=true");
                apiUrl.append("&hourly=").append(joinedParams);
            } else {
                apiUrl.append("&current_weather=true");
            }

            // Tworzymy połączenie HTTP i wykonujemy żądanie GET
            URL url = new URL(apiUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Obsługa błędów sieciowych
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + responseCode);
            }

            // Odczyt odpowiedzi z API
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsowanie odpowiedzi JSON
            JSONObject json = new JSONObject(response.toString());
            JSONObject currentWeather = json.getJSONObject("current_weather");
            StringBuilder result = new StringBuilder();

            // Pobranie wybranych aktualnych danych pogodowych
            if (parameters.contains("temperature_2m") && currentWeather.has("temperature")) {
                result.append("Temperatura: ").append(currentWeather.getDouble("temperature")).append(" °C\n");
            }
            if (parameters.contains("windspeed_10m") && currentWeather.has("windspeed")) {
                result.append("Prędkość wiatru: ").append(currentWeather.getDouble("windspeed")).append(" km/h\n");
            }

            // Dane godzinowe (np. ciśnienie, opady, temperatura gleby)
            if (json.has("hourly")) {
                JSONObject hourly = json.getJSONObject("hourly");
                JSONArray timeArray = hourly.getJSONArray("time");
                int latestIndex = timeArray.length() - 1; // pobieramy ostatnią godzinę

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

            // Jeśli brak danych, zwracamy informację
            if (result.isEmpty()) {
                result.append("Brak danych pogodowych dla wybranych parametrów.");
            }

            String resultStr = result.toString();
            jedis.setex(cacheKey, DEFAULT_TTL_SECONDS, resultStr);
            return resultStr;

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania pogody.";
        }
    }

    // METODA 2: Pobiera prognozę pogody w przedziale dat (z API)
    public String getWeatherForecast(double latitude, double longitude, List<String> parameters, String startDate, String endDate) {
        String cacheKey = generateCacheKey("forecast", latitude, longitude, parameters, startDate, endDate);
        String cached = jedis.get(cacheKey);
        if (cached != null) {
            return cached + " (z cache)\n";}
        try {
            // Budujemy URL zapytania z datami
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);
            apiUrl.append("&start_date=").append(startDate);
            apiUrl.append("&end_date=").append(endDate);

            // Dołączamy wybrane parametry
            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&hourly=").append(joinedParams);
            }

            // Połączenie HTTP
            URL url = new URL(apiUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Obsługa błędu HTTP
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : " + responseCode);
            }

            // Odczyt odpowiedzi JSON
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsowanie danych
            JSONObject json = new JSONObject(response.toString());

            if (!json.has("hourly")) {
                return "Brak danych prognozy pogodowej dla wybranych parametrów.";
            }

            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");

            StringBuilder result = new StringBuilder();

            // Iteracja przez godziny prognozy
            for (int i = 0; i < timeArray.length(); i++) {
                String time = timeArray.getString(i);
                LocalDateTime dateTime = LocalDateTime.parse(time);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("pl"));
                String formattedTime = dateTime.format(formatter);

                result.append(formattedTime).append(":\n");

                // Dla każdego parametru wypisujemy jego wartość
                for (String param : parameters) {
                    if (hourly.has(param)) {
                        JSONArray paramArray = hourly.getJSONArray(param);
                        Object val = paramArray.get(i);

                        String formattedValue;
                        if (val == JSONObject.NULL) {
                            formattedValue = "brak danych";
                        } else if (val instanceof Number number) {
                            if (param.toLowerCase().contains("precipitation") && number.doubleValue() == 0.0) {
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

            String resultStr = result.toString();
            jedis.setex(cacheKey, DEFAULT_TTL_SECONDS, resultStr);
            return resultStr;

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania prognozy pogody. Jeśli pobierasz prognozę pogody, spróbuj zmniejszając liczbę dni";
        }
    }

    // METODA 3: Pobiera dane historyczne pogodowe z ostatnich N dni
    public String getHistoricalWeather(double latitude, double longitude, List<String> parameters, int pastDays) {
        String cacheKey = generateCacheKey("history", latitude, longitude, parameters, String.valueOf(pastDays));
        String cached = jedis.get(cacheKey);
        if (cached != null) {
            return cached + " (z cache)\n";
        }
        try {
            // Budujemy URL z parametrem past_days
            StringBuilder apiUrl = new StringBuilder("https://api.open-meteo.com/v1/forecast");
            apiUrl.append("?latitude=").append(latitude);
            apiUrl.append("&longitude=").append(longitude);
            apiUrl.append("&past_days=").append(pastDays);
            if (!parameters.isEmpty()) {
                String joinedParams = String.join(",", parameters);
                apiUrl.append("&hourly=").append(joinedParams);
            }

            // Połączenie i pobranie danych
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

            // Parsowanie odpowiedzi
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

                // Pomijamy dane z dzisiaj i przyszłości
                if (dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(1))) {
                    continue;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("pl"));
                String formattedTime = dateTime.format(formatter);
                result.append(formattedTime).append(":\n");

                // Wypisywanie wartości parametrów
                for (String param : parameters) {
                    if (hourly.has(param)) {
                        JSONArray paramArray = hourly.getJSONArray(param);
                        Object val = paramArray.get(i);

                        String formattedValue;
                        if (val == JSONObject.NULL) {
                            formattedValue = "brak danych";
                        } else if (val instanceof Number number) {
                            if (param.toLowerCase().contains("precipitation") && number.doubleValue() == 0.0) {
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

            String resultStr = result.toString();
            jedis.setex(cacheKey, 43200, resultStr); // 12h TTL
            return resultStr;

        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd podczas pobierania danych historycznych.";
        }
    }
}
