package com.tomaszwejner.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Klasa odpowiedzialna za geokodowanie i odwrotne geokodowanie z wykorzystaniem API Nominatim OpenStreetMap
public class GeoCodingService {

    /**
     * Metoda pobiera współrzędne geograficzne (latitude, longitude) na podstawie nazwy miasta.
     * Wykonuje zapytanie HTTP GET do Nominatim z parametrem formatu JSON i limit=1.
     *
     * @param city nazwa miasta (np. "Warszawa")
     * @return Coordinates obiekt zawierający szerokość i długość geograficzną
     * @throws Exception w przypadku problemów z połączeniem lub braku wyników
     */
    public Coordinates getCoordinates(String city) throws Exception {
        // Przygotowanie URL do zapytania (zamiana spacji na %20)
        String urlStr = "https://nominatim.openstreetmap.org/search?q="
                + city.replace(" ", "%20")
                + "&format=json&limit=1";

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Nominatim wymaga ustawienia User-Agent (możesz podać dane swojej aplikacji)
        connection.setRequestProperty("User-Agent", "TwojaAplikacjaPogodowa/1.0 (kontakt@twojadomena.pl)");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            // Rzucamy wyjątek w przypadku błędnej odpowiedzi serwera HTTP
            throw new RuntimeException("Failed to get geocode data: HTTP " + responseCode);
        }

        // Czytanie odpowiedzi z serwera
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parsowanie odpowiedzi JSON - spodziewamy się tablicy wyników
        JSONArray arr = new JSONArray(response.toString());
        if (arr.length() == 0) {
            // Brak wyników geokodowania - rzucamy wyjątek
            throw new RuntimeException("City not found");
        }

        JSONObject obj = arr.getJSONObject(0);

        // Pobieramy szerokość i długość geograficzną jako double
        double lat = obj.getDouble("lat");
        double lon = obj.getDouble("lon");

        // Zwracamy obiekt Coordinates z pobranymi wartościami
        return new Coordinates(lat, lon);
    }

    /**
     * Metoda odwrotnego geokodowania - na podstawie współrzędnych zwraca nazwę miasta.
     * Wykonuje zapytanie do Nominatim z parametrami lat, lon, format=json i językiem polskim.
     *
     * @param lat szerokość geograficzna
     * @param lon długość geograficzna
     * @return nazwa miasta lub innej jednostki administracyjnej (np. miejscowości)
     * @throws Exception w przypadku błędów lub braku wyników
     */
    public String getCityName(double lat, double lon) throws Exception {
        // Budujemy URL dla zapytania reverse geocode
        String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                + lat + "&lon=" + lon + "&accept-language=pl";

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "TwojaAplikacjaPogodowa/1.0 (kontakt@twojadomena.pl)");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to get reverse geocode data: HTTP " + responseCode);
        }

        // Odczyt odpowiedzi serwera
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject obj = new JSONObject(response.toString());

        if (!obj.has("address")) {
            // Brak informacji o adresie w odpowiedzi
            throw new RuntimeException("No address found for coordinates");
        }

        JSONObject address = obj.getJSONObject("address");

        // Sprawdzamy różne możliwe pola adresowe, by zwrócić najbardziej szczegółową nazwę miejsca
        if (address.has("city")) return address.getString("city");
        else if (address.has("town")) return address.getString("town");
        else if (address.has("village")) return address.getString("village");
        else if (address.has("hamlet")) return address.getString("hamlet");
        else if (address.has("county")) return address.getString("county");

        // Jeśli nic nie znaleźliśmy, zwracamy wartość domyślną
        return "Nieznana lokalizacja";
    }
}
