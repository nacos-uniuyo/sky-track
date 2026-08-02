package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.CorridorBundle;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.service.CorridorService;
import com.skywatch.weather.service.GreetingService;
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
public class CorridorController {

    private final CorridorService corridorService;
    private final WeatherService weatherService;
    private final GreetingService greetingService;
    private final CurrentUserResolver currentUserResolver;

    public CorridorController(
            CorridorService corridorService,
            WeatherService weatherService,
            GreetingService greetingService,
            CurrentUserResolver currentUserResolver
    ) {
        this.corridorService = corridorService;
        this.weatherService = weatherService;
        this.greetingService = greetingService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/corridor")
    public String corridor(Authentication authentication, Model model) {
        AppUser user = currentUserResolver.currentUser(authentication);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        return "corridor";
    }

    @PostMapping("/corridor")
    public String generate(
            Authentication authentication,
            @RequestParam String origin,
            @RequestParam String destination,
            Model model
    ) {
        AppUser user = currentUserResolver.currentUser(authentication);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));

        CorridorBundle bundle = buildCorridor(origin, destination);
        model.addAttribute("corridor", bundle);
        model.addAttribute("originQuery", origin);
        model.addAttribute("destinationQuery", destination);
        return "corridor";
    }

    private CorridorBundle buildCorridor(String originQuery, String destinationQuery) {
        List<GeoCityResponse> originCities = weatherService.searchCities(originQuery, 1);
        List<GeoCityResponse> destinationCities = weatherService.searchCities(destinationQuery, 1);

        if (originCities.isEmpty() || destinationCities.isEmpty()) {
            return new CorridorBundle(originQuery, destinationQuery, List.of());
        }

        GeoCityResponse origin = originCities.get(0);
        GeoCityResponse destination = destinationCities.get(0);

        return corridorService.generate(
                origin.name(),
                origin.lat(),
                origin.lon(),
                destination.name(),
                destination.lat(),
                destination.lon()
        );
    }
}
