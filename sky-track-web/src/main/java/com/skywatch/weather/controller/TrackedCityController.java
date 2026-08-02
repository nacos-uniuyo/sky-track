package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.TrackedCityRequest;
import com.skywatch.weather.service.TrackedCityService;
import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrackedCityController {

    private final TrackedCityService trackedCityService;
    private final CurrentUserResolver currentUserResolver;

    public TrackedCityController(
            TrackedCityService trackedCityService,
            CurrentUserResolver currentUserResolver
    ) {
        this.trackedCityService = trackedCityService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping("/cities")
    public String add(
            Authentication authentication,
            @RequestParam String cityName,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String countryCode,
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        TrackedCityRequest request = new TrackedCityRequest(
                cityName,
                country,
                countryCode,
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude)
        );
        trackedCityService.addCity(currentUserResolver.currentUser(authentication), request);
        return "redirect:/";
    }

    @PostMapping("/cities/{id}/remove")
    public String remove(Authentication authentication, @PathVariable Long id) {
        trackedCityService.removeCity(currentUserResolver.currentUser(authentication), id);
        return "redirect:/";
    }

    @PostMapping("/cities/{id}/primary")
    public String setPrimary(Authentication authentication, @PathVariable Long id) {
        trackedCityService.setPrimary(currentUserResolver.currentUser(authentication), id);
        return "redirect:/";
    }
}
