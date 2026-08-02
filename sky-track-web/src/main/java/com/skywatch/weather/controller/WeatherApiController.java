package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.DeltaView;
import com.skywatch.weather.dto.ForecastBundle;
import com.skywatch.weather.dto.SnapshotView;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.service.DashboardService;
import com.skywatch.weather.service.DeltaService;
import com.skywatch.weather.service.TrackedCityService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    private final CurrentUserResolver currentUserResolver;
    private final TrackedCityService trackedCityService;
    private final DeltaService deltaService;
    private final DashboardService dashboardService;

    public WeatherApiController(
            CurrentUserResolver currentUserResolver,
            TrackedCityService trackedCityService,
            DeltaService deltaService,
            DashboardService dashboardService
    ) {
        this.currentUserResolver = currentUserResolver;
        this.trackedCityService = trackedCityService;
        this.deltaService = deltaService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{cityId}/delta")
    public ResponseEntity<DeltaView> delta(Authentication authentication, @PathVariable Long cityId) {
        AppUser user = currentUserResolver.currentUser(authentication);
        Optional<TrackedCity> city = trackedCityService.findCity(user, cityId);
        if (city.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return deltaService.deltaFor(cityId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{cityId}/forecast")
    public ResponseEntity<ForecastBundle> forecast(Authentication authentication, @PathVariable Long cityId) {
        AppUser user = currentUserResolver.currentUser(authentication);
        Optional<TrackedCity> city = trackedCityService.findCity(user, cityId);
        if (city.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dashboardService.forecastFor(city.get()));
    }

    @GetMapping("/{cityId}/snapshots")
    public ResponseEntity<List<SnapshotView>> snapshots(
            Authentication authentication,
            @PathVariable Long cityId,
            @RequestParam(defaultValue = "7") int days
    ) {
        AppUser user = currentUserResolver.currentUser(authentication);
        Optional<TrackedCity> city = trackedCityService.findCity(user, cityId);
        if (city.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dashboardService.historyFor(city.get(), days));
    }
}
