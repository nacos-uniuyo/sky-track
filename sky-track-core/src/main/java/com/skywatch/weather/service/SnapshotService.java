package com.skywatch.weather.service;

import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.TrackedCityRepository;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapshotService {

    private final TrackedCityRepository trackedCityRepository;
    private final WeatherSnapshotRepository weatherSnapshotRepository;
    private final WeatherService weatherService;
    private final WeatherAlertService weatherAlertService;

    public SnapshotService(
            TrackedCityRepository trackedCityRepository,
            WeatherSnapshotRepository weatherSnapshotRepository,
            WeatherService weatherService,
            WeatherAlertService weatherAlertService
    ) {
        this.trackedCityRepository = trackedCityRepository;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
        this.weatherService = weatherService;
        this.weatherAlertService = weatherAlertService;
    }

    @Scheduled(fixedRateString = "${weather.snapshot.fixed-rate-ms:1800000}", initialDelay = 60000)
    @Transactional
    public void ingestAllCities() {
        trackedCityRepository.findAll().forEach(this::ingestCity);
    }

    @Transactional
    public WeatherSnapshot ingestCity(TrackedCity city) {
        CurrentWeatherResponse response = weatherService.currentWeather(
                city.getLatitude(), city.getLongitude());

        WeatherSnapshot snapshot = new WeatherSnapshot();
        snapshot.setTrackedCity(city);
        if (response.main() != null) {
            snapshot.setTemperature(response.main().temp());
            snapshot.setFeelsLike(response.main().feels_like());
            snapshot.setHumidity(response.main().humidity());
            snapshot.setPressure(response.main().pressure());
        }
        if (response.wind() != null) {
            snapshot.setWindSpeed(response.wind().speed());
            snapshot.setWindDirection(response.wind().deg());
        }
        snapshot.setVisibility(response.visibility());
        if (!response.weather().isEmpty()) {
            snapshot.setConditionCode(response.weather().get(0).id());
            snapshot.setConditionText(response.weather().get(0).description());
        }
        if (response.sys() != null) {
            snapshot.setSunrise(fromEpoch(response.sys().sunrise()));
            snapshot.setSunset(fromEpoch(response.sys().sunset()));
        }
        snapshot.setRecordedAt(OffsetDateTime.now(ZoneOffset.UTC));

        WeatherSnapshot saved = weatherSnapshotRepository.save(snapshot);
        weatherAlertService.evaluate(city, saved);
        return saved;
    }

    private OffsetDateTime fromEpoch(Long epoch) {
        return epoch != null
                ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC)
                : null;
    }
}
