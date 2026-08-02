package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.model.WeatherAlert;
import com.skywatch.weather.repository.WeatherAlertRepository;
import com.skywatch.weather.service.TrackedCityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertApiController {

    private final CurrentUserResolver currentUserResolver;
    private final WeatherAlertRepository weatherAlertRepository;
    private final TrackedCityService trackedCityService;

    public AlertApiController(
            CurrentUserResolver currentUserResolver,
            WeatherAlertRepository weatherAlertRepository,
            TrackedCityService trackedCityService
    ) {
        this.currentUserResolver = currentUserResolver;
        this.weatherAlertRepository = weatherAlertRepository;
        this.trackedCityService = trackedCityService;
    }

    @GetMapping
    public List<WeatherAlert> list(Authentication authentication) {
        AppUser user = currentUserResolver.currentUser(authentication);
        return weatherAlertRepository.findAllByTrackedCityUserIdOrderByDetectedAtDesc(user.getId());
    }

    @GetMapping("/{cityId}")
    public List<WeatherAlert> listForCity(Authentication authentication, @PathVariable Long cityId) {
        AppUser user = currentUserResolver.currentUser(authentication);
        if (trackedCityService.findCity(user, cityId).isEmpty()) {
            return List.of();
        }
        return weatherAlertRepository.findByTrackedCityIdOrderByDetectedAtDesc(cityId);
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<Void> acknowledge(Authentication authentication, @PathVariable Long id) {
        AppUser user = currentUserResolver.currentUser(authentication);
        if (!weatherAlertRepository.existsByIdAndTrackedCityUserId(id, user.getId())) {
            return ResponseEntity.notFound().build();
        }
        weatherAlertRepository.findById(id).ifPresent(alert -> {
            alert.setAcknowledged(true);
            weatherAlertRepository.save(alert);
        });
        return ResponseEntity.ok().build();
    }
}
