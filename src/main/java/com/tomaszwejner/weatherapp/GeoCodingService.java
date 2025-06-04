package com.tomaszwejner.weatherapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeoCodingService {

    // Metoda: miasto → współrzędne
    public Coordinates getCoordinates(String city) throws Exception {
        String urlStr = "https://nominatim.openstreetmap.org/search?q="
                + city.replace(" ", "%20")
                + "&format=json&limit=1";

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Wymagany nagłówek User-Agent
        connection.setRequestProperty("User-Agent", "TwojaAplikacjaPogodowa/1.0 (kontakt@twojadomena.pl)");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to get geocode data: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONArray arr = new JSONArray(response.toString());
        if (arr.length() == 0) {
            throw new RuntimeException("City not found");
        }

        JSONObject obj = arr.getJSONObject(0);

        double lat = obj.getDouble("lat");
        double lon = obj.getDouble("lon");

        return new Coordinates(lat, lon);
    }

    // Metoda: współrzędne → nazwa miasta (reverse geocoding)
    public String getCityName(double lat, double lon) throws Exception {
        String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                + lat + "&lon=" + lon;

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "TwojaAplikacjaPogodowa/1.0 (kontakt@twojadomena.pl)");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to get reverse geocode data: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject obj = new JSONObject(response.toString());

        if (!obj.has("address")) {
            throw new RuntimeException("No address found for coordinates");
        }

        JSONObject address = obj.getJSONObject("address");

        if (address.has("city")) return address.getString("city");
        else if (address.has("town")) return address.getString("town");
        else if (address.has("village")) return address.getString("village");
        else if (address.has("hamlet")) return address.getString("hamlet");
        else if (address.has("county")) return address.getString("county");

        return "Nieznana lokalizacja"; // fallback
    }
}
