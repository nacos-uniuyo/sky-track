package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.TrackedCityRequest;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.service.GreetingService;
import com.skywatch.weather.service.TrackedCityService;
import com.skywatch.weather.service.WeatherService;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OnboardingController {

    private final TrackedCityService trackedCityService;
    private final WeatherService weatherService;
    private final GreetingService greetingService;
    private final CurrentUserResolver currentUserResolver;

    public OnboardingController(
            TrackedCityService trackedCityService,
            WeatherService weatherService,
            GreetingService greetingService,
            CurrentUserResolver currentUserResolver
    ) {
        this.trackedCityService = trackedCityService;
        this.weatherService = weatherService;
        this.greetingService = greetingService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/onboarding")
    public String onboarding(Authentication authentication, Model model) {
        AppUser user = currentUserResolver.currentUser(authentication);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        model.addAttribute("results", List.of());
        return "onboarding";
    }

    @PostMapping("/onboarding/search")
    public String search(
            Authentication authentication,
            @RequestParam String query,
            Model model
    ) {
        AppUser user = currentUserResolver.currentUser(authentication);
        List<GeoCityResponse> results = weatherService.searchCities(query, 5);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "onboarding";
    }

    @PostMapping("/onboarding/select")
    public String select(
            Authentication authentication,
            @RequestParam String cityName,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String countryCode,
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        AppUser user = currentUserResolver.currentUser(authentication);
        TrackedCityRequest request = new TrackedCityRequest(
                cityName,
                country,
                countryCode,
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude)
        );
        trackedCityService.savePrimaryCity(user, request);
        return "redirect:/";
    }
}
