package com.skywatch.weather.service;

import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherAlert;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.WeatherAlertRepository;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherAlertService {

    private static final BigDecimal PRESSURE_DROP_THRESHOLD = BigDecimal.valueOf(5);
    private static final BigDecimal WIND_SURGE_THRESHOLD = BigDecimal.valueOf(12);

    private final WeatherAlertRepository weatherAlertRepository;
    private final WeatherSnapshotRepository weatherSnapshotRepository;

    public WeatherAlertService(
            WeatherAlertRepository weatherAlertRepository,
            WeatherSnapshotRepository weatherSnapshotRepository
    ) {
        this.weatherAlertRepository = weatherAlertRepository;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
    }

    @Transactional
    public void evaluate(TrackedCity city, WeatherSnapshot latest) {
        Optional<WeatherSnapshot> previousOpt = weatherSnapshotRepository
                .findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                        city.getId(), latest.getRecordedAt());
        if (previousOpt.isEmpty()) {
            return;
        }

        WeatherSnapshot previous = previousOpt.get();
        checkPressureDrop(city, latest, previous);
        checkWindSurge(city, latest, previous);
    }

    private void checkPressureDrop(TrackedCity city, WeatherSnapshot latest, WeatherSnapshot previous) {
        if (latest.getPressure() == null || previous.getPressure() == null) {
            return;
        }
        BigDecimal drop = BigDecimal.valueOf(previous.getPressure() - latest.getPressure());
        if (drop.compareTo(PRESSURE_DROP_THRESHOLD) >= 0) {
            createAlert(city, "PRESSURE_DROP", "HIGH",
                    "Rapid pressure drop",
                    "Pressure fell " + drop + " hPa for " + city.getCityName());
        }
    }

    private void checkWindSurge(TrackedCity city, WeatherSnapshot latest, WeatherSnapshot previous) {
        if (latest.getWindSpeed() == null || previous.getWindSpeed() == null) {
            return;
        }
        BigDecimal surge = latest.getWindSpeed().subtract(previous.getWindSpeed());
        if (surge.compareTo(WIND_SURGE_THRESHOLD) >= 0) {
            createAlert(city, "WIND_SURGE", "MEDIUM",
                    "Sudden wind increase",
                    "Wind speed rose " + surge + " m/s for " + city.getCityName());
        }
    }

    private void createAlert(
            TrackedCity city,
            String alertType,
            String severity,
            String title,
            String description
    ) {
        WeatherAlert alert = new WeatherAlert();
        alert.setTrackedCity(city);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setDetectedAt(OffsetDateTime.now());
        alert.setAcknowledged(false);
        weatherAlertRepository.save(alert);
    }
}
