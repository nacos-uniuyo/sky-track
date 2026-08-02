package com.skywatch.weather.service;

import com.skywatch.weather.dto.DailyView;
import com.skywatch.weather.dto.ForecastBundle;
import com.skywatch.weather.dto.HourlyView;
import com.skywatch.weather.dto.SnapshotView;
import com.skywatch.weather.dto.TrackedCityView;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.dto.weather.ForecastResponse;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final TrackedCityService trackedCityService;
    private final WeatherService weatherService;
    private final DeltaService deltaService;
    private final WeatherSnapshotRepository weatherSnapshotRepository;

    public DashboardService(
            TrackedCityService trackedCityService,
            WeatherService weatherService,
            DeltaService deltaService,
            WeatherSnapshotRepository weatherSnapshotRepository
    ) {
        this.trackedCityService = trackedCityService;
        this.weatherService = weatherService;
        this.deltaService = deltaService;
        this.weatherSnapshotRepository = weatherSnapshotRepository;
    }

    public List<TrackedCityView> cityViews(AppUser user) {
        return trackedCityService.listCities(user).stream()
                .map(city -> enrich(city, currentWeather(city)))
                .toList();
    }

    public TrackedCityView primaryCityView(AppUser user) {
        return trackedCityService.findPrimaryCity(user)
                .map(city -> enrich(city, currentWeather(city)))
                .orElse(null);
    }

    public ForecastBundle forecastFor(TrackedCity city) {
        try {
            var response = weatherService.forecast(city.getLatitude(), city.getLongitude());
            List<HourlyView> hourly = response.list().stream()
                    .limit(24)
                    .map(HourlyView::from)
                    .toList();

            Map<LocalDate, List<ForecastResponse.ForecastItem>> grouped = response.list().stream()
                    .collect(Collectors.groupingBy(
                            item -> Instant.ofEpochSecond(item.dt()).atZone(ZoneOffset.UTC).toLocalDate()));

            List<DailyView> daily = grouped.entrySet().stream()
                    .limit(7)
                    .map(entry -> DailyView.fromGroup(entry.getKey(), entry.getValue()))
                    .toList();

            return new ForecastBundle(hourly, daily);
        } catch (RuntimeException exception) {
            return new ForecastBundle(List.of(), List.of());
        }
    }

    public List<SnapshotView> historyFor(TrackedCity city, int days) {
        OffsetDateTime from = OffsetDateTime.now().minusDays(days);
        return weatherSnapshotRepository
                .findByTrackedCityIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                        city.getId(), from, OffsetDateTime.now())
                .stream()
                .map(snapshot -> new SnapshotView(snapshot.getRecordedAt(), snapshot.getTemperature()))
                .toList();
    }

    private TrackedCityView enrich(TrackedCity city, CurrentWeatherResponse weather) {
        TrackedCityView view = TrackedCityView.from(city, weather);
        return deltaService.deltaFor(city.getId())
                .map(delta -> view.withDelta(delta.temperatureDelta(), delta.since()))
                .orElse(view);
    }

    private CurrentWeatherResponse currentWeather(TrackedCity city) {
        try {
            return weatherService.currentWeather(city.getLatitude(), city.getLongitude());
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
