package com.skywatch.weather.service;

import com.skywatch.weather.dto.CorridorBundle;
import com.skywatch.weather.dto.CorridorPoint;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CorridorService {

    private static final int DEFAULT_WAYPOINTS = 5;

    private final WeatherService weatherService;

    public CorridorService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public CorridorBundle generate(
            String originName,
            BigDecimal originLat,
            BigDecimal originLon,
            String destinationName,
            BigDecimal destinationLat,
            BigDecimal destinationLon
    ) {
        List<CorridorPoint> points = new ArrayList<>();

        for (int i = 0; i <= DEFAULT_WAYPOINTS; i++) {
            double fraction = (double) i / DEFAULT_WAYPOINTS;
            BigDecimal lat = originLat.add(destinationLat.subtract(originLat).multiply(BigDecimal.valueOf(fraction)));
            BigDecimal lon = originLon.add(destinationLon.subtract(originLon).multiply(BigDecimal.valueOf(fraction)));

            String label = i == 0 ? originName : i == DEFAULT_WAYPOINTS ? destinationName : "Stop " + i;
            points.add(resolveWeather(label, lat, lon));
        }

        return new CorridorBundle(originName, destinationName, points);
    }

    private CorridorPoint resolveWeather(String label, BigDecimal lat, BigDecimal lon) {
        try {
            CurrentWeatherResponse weather = weatherService.currentWeather(lat, lon);
            Integer conditionCode = !weather.weather().isEmpty() ? weather.weather().get(0).id() : null;
            return new CorridorPoint(
                    label,
                    lat,
                    lon,
                    conditionCode,
                    weather.main() != null ? weather.main().temp() : null
            );
        } catch (RuntimeException exception) {
            return new CorridorPoint(label, lat, lon, null, null);
        }
    }
}
