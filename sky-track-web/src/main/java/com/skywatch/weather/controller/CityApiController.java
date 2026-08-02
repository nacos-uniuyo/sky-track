package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.TrackedCityRequest;
import com.skywatch.weather.dto.TrackedCityView;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import com.skywatch.weather.service.DashboardService;
import com.skywatch.weather.service.TrackedCityService;
import com.skywatch.weather.service.WeatherService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
public class CityApiController {

    private final DashboardService dashboardService;
    private final TrackedCityService trackedCityService;
    private final WeatherService weatherService;
    private final CurrentUserResolver currentUserResolver;

    public CityApiController(
            DashboardService dashboardService,
            TrackedCityService trackedCityService,
            WeatherService weatherService,
            CurrentUserResolver currentUserResolver
    ) {
        this.dashboardService = dashboardService;
        this.trackedCityService = trackedCityService;
        this.weatherService = weatherService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<TrackedCityView> list(Authentication authentication) {
        return dashboardService.cityViews(currentUserResolver.currentUser(authentication));
    }

    @GetMapping("/search")
    public List<GeoCityResponse> search(@RequestParam String q) {
        return weatherService.searchCities(q, 5);
    }

    @PostMapping
    public TrackedCityView add(Authentication authentication, @RequestBody TrackedCityRequest request) {
        var city = trackedCityService.addCity(currentUserResolver.currentUser(authentication), request);
        return dashboardService.cityViews(currentUserResolver.currentUser(authentication)).stream()
                .filter(view -> view.id().equals(city.getId()))
                .findFirst()
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public void remove(Authentication authentication, @PathVariable Long id) {
        trackedCityService.removeCity(currentUserResolver.currentUser(authentication), id);
    }

    @PostMapping("/{id}/primary")
    public void setPrimary(Authentication authentication, @PathVariable Long id) {
        trackedCityService.setPrimary(currentUserResolver.currentUser(authentication), id);
    }

    @PutMapping("/reorder")
    public void reorder(Authentication authentication, @RequestBody List<Long> cityIds) {
        trackedCityService.reorder(currentUserResolver.currentUser(authentication), cityIds);
    }
}
